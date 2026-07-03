package dev.recallforge.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.recallforge.service.MarkdownTopicImporter.ParsedTopic;

class MarkdownTopicImporterTest {

    private final MarkdownTopicImporter importer = new MarkdownTopicImporter();

    @Test
    void singleTopicHeading_usesItsOwnTitleAsFileTitleAndDefaultsRest() {
        String markdown = "### Java Basics\nSome content here.";

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
        String markdown = "## Programming / Java / Core / Basics\n### Variables\nContent about variables.";

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
        String markdown = "## Programming\n### Loops\nLoop content.";

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
        String markdown = "## Programming / Java\n### First Topic\nFirst content.\n### Second Topic\nSecond content.";

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
        String markdown = "### Empty Topic\n\n### Real Topic\nActual content.";

        List<ParsedTopic> topics = importer.parseMarkdown(markdown);

        assertThat(topics).hasSize(1);
        assertThat(topics.get(0).title()).isEqualTo("Real Topic");
    }
}
