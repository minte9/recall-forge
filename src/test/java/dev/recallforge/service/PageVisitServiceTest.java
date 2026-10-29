package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import dev.recallforge.domain.PageVisit;
import dev.recallforge.repository.PageVisitRepository;

@ExtendWith(MockitoExtension.class)
class PageVisitServiceTest {

    private static final String BROWSER_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0 Safari/537.36";

    private static final String OWN_HOST = "recallforge.dev";

    @Mock
    private PageVisitRepository pageVisitRepository;

    private PageVisitService pageVisitService;

    @BeforeEach
    void setUp() {
        pageVisitService = new PageVisitService(pageVisitRepository);
    }

    @Test
    void recordVisit_newBrowserVisitor_savesHashedVisitForToday() {
        pageVisitService.recordVisit("203.0.113.7", BROWSER_UA, null, OWN_HOST);

        ArgumentCaptor<PageVisit> captor = ArgumentCaptor.forClass(PageVisit.class);
        verify(pageVisitRepository).save(captor.capture());

        PageVisit saved = captor.getValue();
        assertThat(saved.getVisitDate()).isEqualTo(LocalDate.now());
        assertThat(saved.getVisitorHash())
            .hasSize(64)
            .doesNotContain("203.0.113.7")
            .doesNotContain("Mozilla");
    }

    @Test
    void recordVisit_sameVisitorTwice_producesSameHash() {
        pageVisitService.recordVisit("203.0.113.7", BROWSER_UA, null, OWN_HOST);
        pageVisitService.recordVisit("203.0.113.7", BROWSER_UA, null, OWN_HOST);

        ArgumentCaptor<PageVisit> captor = ArgumentCaptor.forClass(PageVisit.class);
        verify(pageVisitRepository, times(2)).save(captor.capture());

        List<PageVisit> saved = captor.getAllValues();
        assertThat(saved.get(0).getVisitorHash()).isEqualTo(saved.get(1).getVisitorHash());
    }

    @Test
    void recordVisit_differentIp_producesDifferentHash() {
        pageVisitService.recordVisit("203.0.113.7", BROWSER_UA, null, OWN_HOST);
        pageVisitService.recordVisit("203.0.113.8", BROWSER_UA, null, OWN_HOST);

        ArgumentCaptor<PageVisit> captor = ArgumentCaptor.forClass(PageVisit.class);
        verify(pageVisitRepository, times(2)).save(captor.capture());

        List<PageVisit> saved = captor.getAllValues();
        assertThat(saved.get(0).getVisitorHash()).isNotEqualTo(saved.get(1).getVisitorHash());
    }

    @Test
    void recordVisit_alreadyCountedToday_doesNotSave() {
        when(pageVisitRepository.existsByVisitDateAndVisitorHash(any(), anyString())).thenReturn(true);

        pageVisitService.recordVisit("203.0.113.7", BROWSER_UA, null, OWN_HOST);

        verify(pageVisitRepository, never()).save(any());
    }

    @Test
    void recordVisit_concurrentDuplicateInsert_isSwallowed() {
        when(pageVisitRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        pageVisitService.recordVisit("203.0.113.7", BROWSER_UA, null, OWN_HOST);

        verify(pageVisitRepository).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
        "Mozilla/5.0 (compatible; bingbot/2.0)",
        "facebookexternalhit/1.1",
        "Mozilla/5.0 HeadlessChrome/120.0",
        "curl/8.5.0",
        "python-requests/2.31",
        "   "
    })
    void recordVisit_botOrMissingUserAgent_isIgnored(String userAgent) {
        pageVisitService.recordVisit("203.0.113.7", userAgent, null, OWN_HOST);

        verify(pageVisitRepository, never()).existsByVisitDateAndVisitorHash(any(), anyString());
        verify(pageVisitRepository, never()).save(any());
    }

    @Test
    void recordVisit_externalReferrer_savesOnlyHost() {
        pageVisitService.recordVisit("203.0.113.7", BROWSER_UA,
            "https://News.YCombinator.com/item?id=123&token=secret", OWN_HOST);

        ArgumentCaptor<PageVisit> captor = ArgumentCaptor.forClass(PageVisit.class);
        verify(pageVisitRepository).save(captor.capture());

        assertThat(captor.getValue().getReferrerHost()).isEqualTo("news.ycombinator.com");
    }

    @ParameterizedTest
    @CsvSource({
        "https://www.google.com/search?q=spaced+repetition, www.google.com",
        "http://example.org, example.org",
        "https://RECALLFORGE.dev/privacy.html, ",
        "https://recallforge.dev/, ",
        "android-app://com.google.android.gm/, ",
        "not a url, ",
        "'   ', ",
        "https://, "
    })
    void referrerHost_extractsExternalHostOrNull(String referrer, String expected) {
        assertThat(PageVisitService.referrerHost(referrer, OWN_HOST)).isEqualTo(expected);
    }

    @Test
    void referrerHost_nullReferrer_isNull() {
        assertThat(PageVisitService.referrerHost(null, OWN_HOST)).isNull();
    }
}
