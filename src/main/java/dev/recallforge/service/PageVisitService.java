package dev.recallforge.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import dev.recallforge.domain.PageVisit;
import dev.recallforge.repository.PageVisitRepository;

// Counts unique daily visitors on the public landing page without storing personal data.
// A visitor is identified by HMAC(date + IP + user-agent) under a random key that lives only
// in memory, so stored hashes can't be reversed or linked across days. Trade-off: a restart
// rotates the key, so a visitor seen both before and after it is counted twice that day.
// The referrer is reduced to its host; self-referrals and direct visits store null.
@Service
public class PageVisitService {

    private static final Pattern BOT_USER_AGENT = Pattern.compile(
        "bot|crawl|spider|slurp|preview|headless|monitor|facebookexternalhit|curl|wget|python|java|http",
        Pattern.CASE_INSENSITIVE
    );

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MAX_REFERRER_HOST_LENGTH = 255;

    private final PageVisitRepository pageVisitRepository;
    private final SecretKeySpec hashKey;

    public PageVisitService(PageVisitRepository pageVisitRepository) {
        this.pageVisitRepository = pageVisitRepository;

        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        this.hashKey = new SecretKeySpec(key, HMAC_ALGORITHM);
    }

    public void recordVisit(String ipAddress, String userAgent, String referrer, String ownHost) {
        if (isBot(userAgent)) {
            return;
        }

        LocalDate today = LocalDate.now();
        String visitorHash = hash(today + "|" + ipAddress + "|" + userAgent);

        if (pageVisitRepository.existsByVisitDateAndVisitorHash(today, visitorHash)) {
            return;
        }

        try {
            pageVisitRepository.save(new PageVisit(today, visitorHash, referrerHost(referrer, ownHost)));
        } catch (DataIntegrityViolationException e) {
            // A concurrent request from the same visitor already inserted today's row.
        }
    }

    static boolean isBot(String userAgent) {
        return userAgent == null || userAgent.isBlank() || BOT_USER_AGENT.matcher(userAgent).find();
    }

    static String referrerHost(String referrer, String ownHost) {
        if (referrer == null || referrer.isBlank()) {
            return null;
        }

        String host;
        try {
            URI uri = new URI(referrer.trim());
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                return null;
            }
            host = uri.getHost();
        } catch (URISyntaxException e) {
            return null;
        }

        if (host == null || host.isBlank() || host.length() > MAX_REFERRER_HOST_LENGTH) {
            return null;
        }

        host = host.toLowerCase(Locale.ROOT);
        return host.equalsIgnoreCase(ownHost) ? null : host;
    }

    private String hash(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(hashKey);
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 unavailable", e);
        }
    }
}
