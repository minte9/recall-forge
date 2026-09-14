package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.recallforge.service.MarkdownTopicImporter.ParsedTopic;

class MarkdownTopicImporterTest {

    private final MarkdownTopicImporter importer = new MarkdownTopicImporter();

    @Test
    void singleTopicHeading_usesItsOwnTitleAsFileTitleAndDefaultsRest() {
        String markdown = "## Java Basics\nSome content here.";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(1);
        ParsedTopic topic = topics.get(0);
        assertThat(topic.environment()).isEqualTo("General");
        assertThat(topic.category()).isEqualTo("Uncategorized");
        assertThat(topic.subcategory()).isEqualTo("Uncategorized");
        assertThat(topic.fileTitle()).isEqualTo("Java Basics");
        assertThat(topic.title()).isEqualTo("Java Basics");
        assertThat(topic.content()).isEqualTo("Some content here.");
    }

    @Test
    void pathHeading_assignsEnvironmentCategorySubcategoryAndFileTitle() {
        String markdown = "# Programming / Java / Core / Basics\n## Variables\nContent about variables.";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(1);
        ParsedTopic topic = topics.get(0);
        assertThat(topic.environment()).isEqualTo("Programming");
        assertThat(topic.category()).isEqualTo("Java");
        assertThat(topic.subcategory()).isEqualTo("Core");
        assertThat(topic.fileTitle()).isEqualTo("Basics");
        assertThat(topic.title()).isEqualTo("Variables");
    }

    @Test
    void pathHeadingWithFewerParts_fallsBackToDefaultsAndUsesFirstTopicTitleAsFileTitle() {
        String markdown = "# Programming\n## Loops\nLoop content.";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(1);
        ParsedTopic topic = topics.get(0);
        assertThat(topic.environment()).isEqualTo("Programming");
        assertThat(topic.category()).isEqualTo("Uncategorized");
        assertThat(topic.subcategory()).isEqualTo("Uncategorized");
        assertThat(topic.fileTitle()).isEqualTo("Loops");
    }

    @Test
    void multipleTopicsUnderSamePath_shareTheFirstTopicsTitleAsFileTitle() {
        String markdown = "# Programming / Java\n## First Topic\nFirst content.\n## Second Topic\nSecond content.";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(2);
        assertThat(topics.get(0).fileTitle()).isEqualTo("First Topic");
        assertThat(topics.get(1).fileTitle()).isEqualTo("First Topic");
        assertThat(topics.get(0).title()).isEqualTo("First Topic");
        assertThat(topics.get(1).title()).isEqualTo("Second Topic");
    }

    @Test
    void noHeadings_wholeMarkdownBecomesOneTopicWithDefaultTitles() {
        String markdown = "Just plain text, no headings.";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(1);
        ParsedTopic topic = topics.get(0);
        assertThat(topic.fileTitle()).isEqualTo("Untitled");
        assertThat(topic.title()).isEqualTo("Untitled");
        assertThat(topic.content()).isEqualTo(markdown);
    }

    @Test
    void topicHeadingWithBlankContent_isSkipped() {
        String markdown = "## Empty Topic\n\n## Real Topic\nActual content.";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(1);
        assertThat(topics.get(0).title()).isEqualTo("Real Topic");
    }

    @Test
    void hashCommentInsideFencedCodeBlock_isNotMisparsedAsPathHeadingAndIsKeptInContent() {
        String markdown = "# Programming / Python / Basics / Comments\n"
            + "## First Topic\n"
            + "Explanation.\n"
            + "```python\n"
            + "# a plain comment, not a heading\n"
            + "x = 1\n"
            + "```\n"
            + "## Second Topic\n"
            + "Second content.";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(2);
        assertThat(topics.get(0).title()).isEqualTo("First Topic");
        assertThat(topics.get(0).content()).contains("# a plain comment, not a heading");
        assertThat(topics.get(0).environment()).isEqualTo("Programming");
        assertThat(topics.get(1).title()).isEqualTo("Second Topic");
        assertThat(topics.get(1).environment()).isEqualTo("Programming");
        assertThat(topics.get(1).category()).isEqualTo("Python");
    }

    @Test
    void doubleHashCommentInsideFencedCodeBlock_isNotMisparsedAsTopicHeading() {
        String markdown = "# Programming / Perl / Basics / Comments\n"
            + "## Only Topic\n"
            + "Explanation.\n"
            + "```perl\n"
            + "## a loud comment, not a heading\n"
            + "my $x = 1;\n"
            + "```\n"
            + "Trailing content after the fence.";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(1);
        assertThat(topics.get(0).title()).isEqualTo("Only Topic");
        assertThat(topics.get(0).content()).contains("## a loud comment, not a heading");
        assertThat(topics.get(0).content()).contains("Trailing content after the fence.");
    }

    @Test
    void blankHashLine_isNotMisparsedAsPathHeadingAndDoesNotResetCategory() {
        String markdown = "# JVM / Spring / Framework / AOP\n"
            + "## 1. Aspect-Oriented Programming\n"
            + "Some content.\n"
            + "#  \n"
            + "More content after a stray hard-break hash line.";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(1);
        ParsedTopic topic = topics.get(0);
        assertThat(topic.environment()).isEqualTo("JVM");
        assertThat(topic.category()).isEqualTo("Spring");
        assertThat(topic.subcategory()).isEqualTo("Framework");
        assertThat(topic.fileTitle()).isEqualTo("AOP");
        assertThat(topic.content()).contains("#");
    }

    @Test
    void blankDoubleHashLine_isNotMisparsedAsTopicHeading() {
        String markdown = "# JVM / Spring / Framework / AOP\n"
            + "## Only Topic\n"
            + "Some content.\n"
            + "##  \n"
            + "More content after a stray hard-break double-hash line.";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(1);
        assertThat(topics.get(0).title()).isEqualTo("Only Topic");
        assertThat(topics.get(0).content()).contains("##");
    }

    @Test
    void tildeFence_alsoSuppressesHeadingDetectionInsideIt() {
        String markdown = "# Programming / Bash / Basics / Comments\n"
            + "## Only Topic\n"
            + "Explanation.\n"
            + "~~~bash\n"
            + "# not a heading either\n"
            + "echo hi\n"
            + "~~~";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(1);
        assertThat(topics.get(0).content()).contains("# not a heading either");
    }
}
