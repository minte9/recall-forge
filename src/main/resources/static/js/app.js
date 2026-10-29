const { createApp } = Vue;

const LAST_CATEGORY_STORAGE_KEY = "recallforge.lastCategory";

// Mirrors OpenAiService.trimUserAnswer's maxChars server-side - kept in
// sync manually since there's no shared config between frontend and backend.
const MAX_ANSWER_LENGTH = 2000;

// Starter Pack button rows, in display order. The first category of each
// row is its level 1 category (see StarterPackService). Also sets the order
// of <job-profile-progress> bars.
const STARTER_PACK_ROWS = [
  ["Java", "Spring"],
  ["REST", "GraphQL", "MCP"],
  ["Javascript", "TypeScript", "Angular"],
  ["SQL", "PostgreSQL"],
  ["Python", "Algorithms"],
  ["Git", "Docker"]
];

const app = createApp({
  data() {
    return {
      loading: false,
      error: null,
      success: null,
      currentMarkdownFileId: null,
      currentMarkdownReviewCount: null,
      currentMarkdownCodeChallenges: [],
      viewingCodeChallenges: false,
      newMarkdownContent: "",
      currentCategory: null,
      showMetadataHelp: false,
      markdown: null,
      review: null,
      historyTopic: null,
      notDueTopic: null,
      userAnswer: "",
      result: null,
      dashboard: null,
      markdownSummaries: [],
      leftExpanded: false,
      leftPanelWidth: null,
      answerSubmitted: false,
      isRetry: false,
      currentUser: null,
      menuOpen: false,
      leftPanel: "markdown",
      selectedGroup: null,
      mobileView: "dashboard",
      mobileMenuOpen: false,
      openMarkdownGroup: null,
      mobilePageTitle: "Dashboard",
      markdownLoading: false,
      reviewQuestionLoading: false,
      markdownEditing: false,
      optimizingMarkdown: false,
      optimizingSeconds: 0,
      optimizingIntervalId: null,
      optimizeFinalized: false,
      // { kind: "STRUCTURED" | "GENERATED", originalContent } set once an
      // Optimize preview is accepted, cleared the moment the box is edited
      // again (alongside optimizeFinalized) - tells Submit whether/what to
      // record via AppliedOptimization.
      pendingOptimization: null,
      currentMarkdownOptimized: false,
      currentMarkdownAiGenerated: false,
      currentMarkdownOriginalContent: null,
      viewingOriginal: false,
      editingOriginalMarkdown: false,
      originalMarkdownDraft: "",
      feedbackHistory: [],
      feedbackHistoryVisible: false,
      feedbackHistoryLoading: false,
      feedbackHistoryLoadedForTopicId: null,
      openFeedbackHistoryItemId: null,
      selectedEnvironment: null,
      environmentMenuOpen: false,
      retryMenuOpen: false,
      uploadMenuOpen: false,
      dashboardActionMenuOpen: false,
      codeChallengeLoading: false,
      adminUsers: [],
      adminVisits: [],
      categoryOptions: [],
      starterPackUnlockStatus: {},
      starterPackUnlockThreshold: 30,
      starterPackMastery: {},
      starterPackDue: {},
      starterPackRows: STARTER_PACK_ROWS,
      jobProfiles: [],
      activeJobProfileId: null,
      showDangerZone: false,
      topicsInFile: [],
      topicDropdownOpen: false,
      topicDropdownAlignRight: false,

      initialMarkdown: `
<h3>Markdown File</h3>

Upload a markdown file to start.
The uploaded markdown will be saved in the database.
When you click <strong>Start Review</strong>, this panel will be updated.
`
    };
  },

  computed: {
    rewardProgress() {
      if (!this.dashboard || !this.dashboard.reward) {
        return 0;
      }

      return Math.max(
        0,
        Math.min(100, this.dashboard.reward.masteryPercent || 0)
      );
    },

    codeCoverageProgress() {
      if (!this.dashboard || !this.dashboard.reward) {
        return 0;
      }

      return Math.max(
        0,
        Math.min(100, this.dashboard.reward.codeCoveragePercent || 0)
      );
    },

    isDesktop() {
      return window.innerWidth > 900;
    },

    categoryIconName() {
      return (this.currentCategory && CATEGORY_ICONS[this.currentCategory]) || null;
    },

    dailyLimitReached() {
      return !!this.dashboard && this.dashboard.reviewsToday >= this.dashboard.dailyReviewLimit;
    },

    mobileTitleDisplay() {
      return this.mobilePageTitle;
    },

    isLibraryMode() {
      return this.leftPanel !== "markdown";
    },

    // Single Edit/Show menu slot - the main markdown is always on screen now
    // (KB/CC open as sections under it), so it edits the main markdown, and
    // only acts on the original while the original itself is being edited.
    editToggleLabel() {
      if (this.editingOriginalMarkdown) {
        return "Show Original";
      }

      return this.markdownEditing ? "Show Markdown" : "Edit Markdown";
    },

    // The AI badge is a pure status label now (see index.html) - it isn't
    // reused as a navigation icon anywhere, so this always stays eye/pencil.
    editToggleIcon() {
      if (this.editingOriginalMarkdown) {
        return "eye";
      }

      return this.markdownEditing ? "eye" : "pencil";
    },

    // The topic's actual spaced-repetition memory score (30% old / 70% new
    // blend), not a plain average of past attempt scores - keeps this badge
    // consistent with "Updated memory score" in the feedback card and with
    // file.averageMemoryScore elsewhere in the app.
    topicMemoryScore() {
      if (!this.activeTopic) {
        return null;
      }

      if (this.result && this.result.topicId === this.activeTopic.topicId) {
        return this.result.updateMemoryScore;
      }

      const topic = this.topicsInFile.find(t => t.id === this.activeTopic.topicId);
      return topic ? topic.memoryScore : null;
    },

    // Perfect (score >= 1), non-retry code-challenge XP earned for the
    // active topic - same lookup pattern as topicMemoryScore, since both
    // read from topicsInFile rather than the review/history/notDue DTOs.
    topicCodeChallengeXp() {
      if (!this.activeTopic) {
        return 0;
      }

      const topic = this.topicsInFile.find(t => t.id === this.activeTopic.topicId);
      return topic ? topic.codeChallengeXp : 0;
    },

    // Review streak for the active topic - same lookup pattern as
    // topicMemoryScore/topicCodeChallengeXp. Shown to the user as a
    // Leitner box (streak + 1) on the mastery ring's small badge.
    topicReviewStreak() {
      if (!this.activeTopic) {
        return 0;
      }

      if (this.result && this.result.topicId === this.activeTopic.topicId) {
        return this.result.reviewStreak;
      }

      const topic = this.topicsInFile.find(t => t.id === this.activeTopic.topicId);
      return topic ? topic.reviewStreak : 0;
    },

    // A score of 0.7 or above moves the topic up one box, anything lower
    // moves it down one - keep in sync with
    // RepetitionService.STREAK_SUCCESS_THRESHOLD.
    resultBoxAdvanced() {
      return !!this.result && this.result.score >= 0.7;
    },

    // "Advanced to box N" / "Back to box N" on the result screen.
    resultBoxMessage() {
      if (!this.result) {
        return "";
      }

      const box = this.result.reviewStreak + 1;
      return this.resultBoxAdvanced ? `Advanced to box ${box}` : `Back to box ${box}`;
    },

    aiEnabled() {
      return this.currentUser ? this.currentUser.aiEnabled !== false : true;
    },

    activeTopic() {
      return this.review || this.historyTopic || this.notDueTopic;
    },

    activeJobProfile() {
      return this.jobProfiles.find(p => p.id === this.activeJobProfileId) || null;
    },

    // Category names of the active job profile, or null for "All Categories".
    // Matched by name only - starter pack buttons don't know their environment.
    activeProfileCategories() {
      return this.activeJobProfile
        ? this.activeJobProfile.categories.map(c => c.category)
        : null;
    },

    // Simple average of the <job-profile-progress> bars' reviewed % (locked
    // and not-imported categories count as 0), or null for "All Categories".
    activeProfileReadiness() {
      if (!this.activeProfileCategories) {
        return null;
      }

      const categories = STARTER_PACK_ROWS.flat()
        .filter(c => this.activeProfileCategories.includes(c));

      if (categories.length === 0) {
        return null;
      }

      const total = categories.reduce((sum, c) => sum + (this.starterPackMastery[c] || 0), 0);

      return Math.round(total / categories.length);
    },

    environmentGroups() {
      const groups = {};

      this.categoryOptions.forEach(option => {
        if (!groups[option.environment]) {
          groups[option.environment] = [];
        }

        groups[option.environment].push({
          category: option.category,
          isPrivate: option.isPrivate,
          dueCount: option.dueCount,
          started: option.started
        });
      });

      const priorityOrder = ["JVM", "PYTHON", "DEVOPS", "DB"];

      const orderedNames = Object.keys(groups).sort((a, b) => {
        const priorityA = priorityOrder.indexOf(a.toUpperCase());
        const priorityB = priorityOrder.indexOf(b.toUpperCase());

        if (priorityA !== -1 && priorityB !== -1) {
          return priorityA - priorityB;
        }

        if (priorityA !== -1) {
          return -1;
        }

        if (priorityB !== -1) {
          return 1;
        }

        return a.localeCompare(b);
      });

      const orderedGroups = {};

      orderedNames.forEach(name => {
        orderedGroups[name] = groups[name];
      });

      return orderedGroups;
    },

    filteredMarkdownGroups() {
      const groups = {};

      this.markdownSummaries
        .filter(file => file.category === this.selectedEnvironment)
        .forEach(file => {
          const key = file.subcategory;

          if (!groups[key]) {
            groups[key] = {
              environment: file.environment,
              files: []
            };
          }

          groups[key].files.push(file);
        });

      return groups;
    },

    // Flattens filteredMarkdownGroups (subcategory groups, in the same
    // order the sidebar renders them) into a single ordered list of file
    // IDs for the current category, used for the prev/next nav buttons.
    currentCategoryMarkdownFileIds() {
      return Object.values(this.filteredMarkdownGroups)
        .flatMap(group => group.files.map(file => file.markdownFileId));
    },

    previousMarkdownFileId() {
      const ids = this.currentCategoryMarkdownFileIds;
      const index = ids.indexOf(this.currentMarkdownFileId);
      return index > 0 ? ids[index - 1] : null;
    },

    nextMarkdownFileId() {
      const ids = this.currentCategoryMarkdownFileIds;
      const index = ids.indexOf(this.currentMarkdownFileId);
      return index !== -1 && index < ids.length - 1 ? ids[index + 1] : null;
    },

    // The current file's code challenges, ordered by the topic's leading
    // number (stable, so same-topic challenges keep their reviewedAt
    // order), with a showTopicHeader flag marking the first challenge of
    // each topic group so the template can show the topic title once above
    // the group.
    codeChallengesList() {
      const topicNumber = (title) => {
        const m = (title || "").match(/^\s*(\d+)\./);
        return m ? parseInt(m[1], 10) : null;
      };

      let prevTopicId = null;

      return this.currentMarkdownCodeChallenges
        .slice()
        .sort((a, b) => {
          const na = topicNumber(a.topicTitle), nb = topicNumber(b.topicTitle);
          if (na !== null && nb !== null && na !== nb) return na - nb;
          return (a.topicTitle || "").localeCompare(b.topicTitle || "");
        })
        .map(c => {
          const showTopicHeader = prevTopicId !== c.topicId;
          prevTopicId = c.topicId;

          return {
            ...c,
            showTopicHeader,
            // topicHeading: (c.topicTitle || "").replace(/^\s*\d+\.\s*/, "")
            topicHeading: (c.topicTitle || "")
          };
        });
    }
  },

  async mounted() {
    document.addEventListener("click", this.handleBodyClick);
    document.addEventListener("keydown", this.handleGlobalKeydown);

    await this.loadCategoryOptions();
    await this.loadDashboard();
    await this.loadCurrentUser();
    await this.loadJobProfiles();
    // Desktop opens on the Starter Pack; mobile keeps its Dashboard default.
    await this.loadMarkdownSummaries(!this.isDesktop);

    this.autoOpenSingleMarkdownGroup();

    if (this.isDesktop) {
      await this.showStarterPack();
    } else if (this.categoryOptions.length === 0) {
      this.showMarkdownSummaries();
    }
  },

  beforeUnmount() {
    document.removeEventListener("click", this.handleBodyClick);
    document.removeEventListener("keydown", this.handleGlobalKeydown);
  },

  methods: {
    // Drag-resize the left/right desktop panels. Clamps to a 240px minimum
    // on each side (matches .layout's grid-template-columns minmax bounds
    // in app.css) so neither panel can be dragged into uselessness.
    startPanelResize(event) {
      event.preventDefault();

      const layoutEl = event.currentTarget.closest(".layout");
      if (!layoutEl) return;

      const rect = layoutEl.getBoundingClientRect();
      const minPx = 240;
      const gutterPx = 12;

      const onMove = (moveEvent) => {
        const maxX = rect.width - gutterPx - minPx;
        const x = Math.min(Math.max(moveEvent.clientX - rect.left, minPx), maxX);
        this.leftPanelWidth = (x / rect.width) * 100;
      };

      const onUp = () => {
        document.removeEventListener("mousemove", onMove);
        document.removeEventListener("mouseup", onUp);
        document.body.classList.remove("panel-resizing");
      };

      document.body.classList.add("panel-resizing");
      document.addEventListener("mousemove", onMove);
      document.addEventListener("mouseup", onUp);
    },

    resetPanelWidth() {
      this.leftPanelWidth = null;
    },

    formatScore(score) {
      return Number(score).toFixed(2);
    },

    // Rounds the same way formatScore displays before comparing, so a score
    // like 0.696 (displayed "0.70") is colored high-score instead of low.
    isHighScore(score) {
      return Number(Number(score).toFixed(2)) >= 0.7;
    },

    // Dash offset for the mastery ring's progress circle - r=23 matches
    // the <circle> radius in the ring markup, so this stays in sync with
    // .mastery-ring-progress's stroke-dasharray in app.css.
    masteryRingOffset(score) {
      const circumference = 2 * Math.PI * 23;
      const clamped = Math.max(0, Math.min(1, Number(score) || 0));
      return circumference * (1 - clamped);
    },

    formatTokenCount(value) {
      const tokens = Number(value) || 0;

      if (tokens < 1000) {
        return String(tokens);
      }

      return (tokens / 1000).toFixed(1).replace(/\.0$/, "") + "k";
    },

    truncateTitle(title, maxLength = 30) {
      if (!title || title.length <= maxLength) {
        return title;
      }

      return title.slice(0, maxLength) + "...";
    },

    formatDate(date) {
      return new Date(date).toLocaleDateString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric"
      });
    },

    formatDateTime(value) {
      if (!value) return "";
      return new Date(value).toLocaleString();
    },

    formatShortDate(value) {
      if (!value) return "";

      return new Date(value).toLocaleString(undefined, {
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
      });
    },

    formatSubcategory(name) {
      if (!name) return "";

      return name
        .toLowerCase()
        .replace(/\b\w/g, c => c.toUpperCase());
    },

    handleBodyClick(event) {
      if (!this.mobileMenuOpen) {
        return;
      }

      const menu = this.$refs.mobileMenu;

      if (menu && !menu.contains(event.target)) {
        this.mobileMenuOpen = false;
      }
    },

    handleGlobalKeydown(event) {
      if (!event.ctrlKey || !event.shiftKey) {
        return;
      }

      const key = event.key.toLowerCase();

      if (key === "c") {
        if (this.leftPanel === "markdown" && !this.markdownLoading && this.currentMarkdownFileId &&
            this.currentMarkdownCodeChallenges.length > 0 && !this.viewingCodeChallenges) {
          event.preventDefault();
          this.showCodeChallenges();
        }
        return;
      }

      if (key === "m") {
        if (this.leftPanel === "markdown" && !this.markdownLoading && this.currentMarkdownFileId &&
            this.viewingCodeChallenges) {
          event.preventDefault();
          this.hideCodeChallenges();
        }
        return;
      }

      if (key === "h") {
        if (this.leftPanel === "markdown" && !this.markdownLoading && this.currentMarkdownFileId) {
          event.preventDefault();
          this.showReviewsHistory();
        }
        return;
      }

      if (key === "e") {
        if (this.leftPanel === "markdown" && !this.markdownLoading && this.currentMarkdownFileId &&
            !this.markdownEditing) {
          event.preventDefault();
          this.editMarkdown();
        }
        return;
      }

      if (event.key !== "Enter") {
        return;
      }

      if (!this.review || !this.answerSubmitted || this.loading || this.dailyLimitReached) {
        return;
      }

      event.preventDefault();
      this.startReview();
    },

    onTitleClick() {
      if (this.mobilePageTitle === "Dashboard") {
        this.showMarkdownSummaries();
      } else
      if (this.mobilePageTitle === "Markdowns") {
        this.showStarterPack();
      } else {
        this.showDashboard();
      }
    },

    renderMarkdown(md, options) {
      return Markdown.render(md, options);
    },

    // v-html only re-renders when the markdown string changes, so reviewing
    // another topic from the same file keeps whatever code blocks were left
    // open. Close them all, then open only the ones under the current topic's
    // ## heading (up to the next h1/h2). Titles are compared with markdown
    // formatting stripped since the rendered heading's text drops it; no
    // match just leaves everything closed.
    openCurrentTopicCode() {
      const container = this.$refs.markdownView;
      if (!container) {
        return;
      }

      container.querySelectorAll("details.code-block").forEach(d => d.open = false);

      const normalize = text => (text || "").replace(/[`*_]/g, "").replace(/\s+/g, " ").trim();
      const title = normalize(this.review && this.review.topicTitle);
      if (!title) {
        return;
      }

      const heading = Array.from(container.querySelectorAll("h2"))
        .find(h => normalize(h.textContent) === title);

      for (let el = heading && heading.nextElementSibling; el && !/^H[12]$/.test(el.tagName); el = el.nextElementSibling) {
        el.querySelectorAll("details.code-block").forEach(d => d.open = true);
        if (el.matches("details.code-block")) {
          el.open = true;
        }
      }
    },

    onMarkdownClick(event) {
      if (event.target.closest(".code-block")) {
        return;
      }

      if (this.currentMarkdownFileId) {
        this.editMarkdown();
      }
    },

    // Original's counterpart to onMarkdownClick.
    onOriginalMarkdownClick(event) {
      if (event.target.closest(".code-block")) {
        return;
      }

      if (this.currentMarkdownFileId) {
        this.editOriginalMarkdown();
      }
    },

    // Backs the single Edit/Show menu button (see editToggleLabel/Icon) -
    // closes the original's editor while it's open, otherwise toggles the
    // main markdown editor. The original itself is edited in place from its
    // section (click it, admin-only) or via Add Original.
    toggleEdit() {
      if (this.editingOriginalMarkdown) {
        this.editingOriginalMarkdown = false;
        return;
      }

      if (this.markdownEditing) {
        this.showMarkdown();
      } else {
        this.editMarkdown();
      }
    },

    editMarkdown() {
      const scrollY = window.scrollY;

      this.leftPanel = "markdown";
      this.markdownEditing = true;
      this.viewingCodeChallenges = false;
      this.viewingOriginal = false;
      this.optimizeFinalized = false;
      this.pendingOptimization = null;

      this.$nextTick(() => {
        this.resizeMarkdownEditor();
        this.$refs.markdownEditor?.focus({ preventScroll: true });
        window.scrollTo(0, scrollY);
      });
    },

    showMarkdown() {
      const scrollY = window.scrollY;

      this.markdownEditing = false;

      this.$nextTick(() => {
        window.scrollTo(0, scrollY);
      });
    },

    // Views the original snapshot captured the first time this file was
    // optimized - independent of (and never overwritten by) the current
    // `content`. No-op for a file that was never optimized.
    showOriginal() {
      if (!this.currentMarkdownOptimized) {
        return;
      }

      const scrollY = window.scrollY;

      this.markdownEditing = false;
      this.viewingCodeChallenges = false;
      this.editingOriginalMarkdown = false;
      this.viewingOriginal = true;

      this.$nextTick(() => {
        window.scrollTo(0, scrollY);
      });
    },

    hideOriginal() {
      const scrollY = window.scrollY;

      this.viewingOriginal = false;
      this.editingOriginalMarkdown = false;

      this.$nextTick(() => {
        window.scrollTo(0, scrollY);
      });
    },

    // KB button / menu - the original opens as a section under the markdown
    // rather than replacing it; only one of KB / CC is open at a time.
    toggleOriginal() {
      if (this.viewingOriginal) {
        this.hideOriginal();
      } else {
        this.showOriginal();
      }
    },

    // Original's own edit form - saves through updateOriginalMarkdown,
    // completely separate from submitMarkdownEdit, so it can never affect
    // the current (optimized) `content`. Also doubles as "Add Original" for
    // a file that has none yet (currentMarkdownOriginalContent is null, so
    // the draft just starts empty) - callable from the main markdown view,
    // not just from within Show Original, hence forcing viewingOriginal.
    editOriginalMarkdown() {
      const scrollY = window.scrollY;

      this.viewingCodeChallenges = false;
      this.viewingOriginal = true;
      this.originalMarkdownDraft = this.currentMarkdownOriginalContent || "";
      this.editingOriginalMarkdown = true;

      this.$nextTick(() => {
        this.resizeMarkdownEditor("originalMarkdownEditor");
        this.$refs.originalMarkdownEditor?.focus({ preventScroll: true });
        window.scrollTo(0, scrollY);
      });
    },

    async submitOriginalMarkdownEdit() {
      if (!this.currentMarkdownFileId) {
        return;
      }

      const scrollY = window.scrollY;

      this.loading = true;
      this.error = null;
      this.success = null;

      try {
        const updated = await Api.updateOriginalMarkdown(this.currentMarkdownFileId, this.originalMarkdownDraft);

        this.currentMarkdownOriginalContent = updated.originalContent || null;
        this.currentMarkdownOptimized = !!updated.optimized;
        this.success = "Original markdown saved.";
        this.editingOriginalMarkdown = false;

        this.$nextTick(() => {
          window.scrollTo(0, scrollY);
        });

      } catch (err) {
        this.error = err.message || "Could not save the original markdown.";
      } finally {
        this.loading = false;
      }
    },

    // Inverse of submitOriginalMarkdownEdit's first save - removes the
    // original entirely (optimized goes back to false) and returns to the
    // main markdown view, since there's nothing left to show/edit here.
    async deleteOriginalMarkdown() {
      if (!this.currentMarkdownFileId) {
        return;
      }

      const confirmed = confirm(
        "Delete the original for this file? This cannot be undone."
      );

      if (!confirmed) {
        return;
      }

      const scrollY = window.scrollY;

      this.loading = true;
      this.error = null;
      this.success = null;

      try {
        const updated = await Api.deleteOriginalMarkdown(this.currentMarkdownFileId);

        this.currentMarkdownOriginalContent = updated.originalContent || null;
        this.currentMarkdownOptimized = !!updated.optimized;
        this.success = "Original markdown deleted.";
        this.editingOriginalMarkdown = false;
        this.viewingOriginal = false;

        this.$nextTick(() => {
          window.scrollTo(0, scrollY);
        });

      } catch (err) {
        this.error = err.message || "Could not delete the original markdown.";
      } finally {
        this.loading = false;
      }
    },

    resizeMarkdownEditor(refName = "markdownEditor") {
      const el = this.$refs[refName];

      if (!el) {
        return;
      }

      const scrollY = window.scrollY;
      el.style.height = "auto";
      el.style.height = el.scrollHeight + "px";
      window.scrollTo(0, scrollY);
    },

    async submitMarkdownEdit() {
      if (!this.currentMarkdownFileId) {
        this.error = "This file can't be saved yet — upload or open a saved markdown file first.";
        return;
      }

      const scrollY = window.scrollY;

      this.loading = true;
      this.error = null;
      this.success = null;

      // Only carry the optimization forward if the box still holds exactly
      // what Optimize returned - any edit since resets optimizeFinalized
      // (and pendingOptimization) to null, so a tweaked AI draft saves as a
      // plain manual edit instead of falsely claiming AI provenance.
      const appliedOptimization =
        this.optimizeFinalized && this.pendingOptimization ? this.pendingOptimization.kind : null;
      const originalContentSnapshot =
        appliedOptimization === "STRUCTURED" ? this.pendingOptimization.originalContent : null;

      try {
        const updated = await Api.updateMarkdown(
          this.currentMarkdownFileId, this.markdown, appliedOptimization, originalContentSnapshot);

        this.markdown = updated.content;
        this.currentMarkdownOptimized = !!updated.optimized;
        this.currentMarkdownAiGenerated = !!updated.aiGenerated;
        this.currentMarkdownOriginalContent = updated.originalContent || null;
        this.pendingOptimization = null;
        this.success = "Markdown saved.";
        this.markdownEditing = false;

        await this.loadTopicsInFile(this.currentMarkdownFileId);

        // The edit may have changed environment/category (e.g. via
        // Optimize on a file that started out Uncategorized), so refresh
        // the dropdown/dashboard/summaries the same way upload/create do.
        await this.loadCategoryOptions();
        await this.loadDashboard();
        await this.loadMarkdownSummaries(false);

        // A renamed topic is deleted and recreated with a new id server-side,
        // so the review panel may still be pointing at a topic that no
        // longer exists — reload it onto a valid topic in that case.
        const reviewedTopicGone =
          this.review &&
          this.review.markdownFileId === this.currentMarkdownFileId &&
          !this.topicsInFile.some(topic => topic.id === this.review.topicId);

        if (reviewedTopicGone) {
          await this.startReview();
        }

        this.$nextTick(() => {
          window.scrollTo(0, scrollY);
        });

      } catch (err) {
        this.error = err.message || "Could not save markdown file.";
      } finally {
        this.loading = false;
      }
    },

    // Shared by the edit panel and the add-file panel — both are plain
    // content-in/content-out textareas, so one method covers both. This
    // only previews the AI's suggestion in the textarea; nothing is saved
    // until the existing Submit button is clicked.
    async optimizeMarkdown() {
      const isAddFile = this.leftPanel === "add-file";
      const content = isAddFile ? this.newMarkdownContent : this.markdown;

      if (!content || !content.trim()) {
        return;
      }

      const confirmed = confirm(
        "Optimize this content with AI? This can take up to a minute and will replace the current text in the editor (not saved until you click Submit)."
      );

      if (!confirmed) {
        return;
      }

      this.optimizingMarkdown = true;
      this.optimizingSeconds = 0;
      this.error = null;
      this.success = null;

      this.optimizingIntervalId = setInterval(() => {
        this.optimizingSeconds++;
      }, 1000);

      try {
        const optimized = await Api.optimizeMarkdown(content);

        // content (the pre-optimize text) is captured now, before it's
        // overwritten below - it's the snapshot Submit will store as the
        // original, only for the STRUCTURED branch (see submitMarkdownEdit/
        // submitAddFile).
        this.pendingOptimization = {
          kind: optimized.structured ? "STRUCTURED" : "GENERATED",
          originalContent: content
        };

        if (isAddFile) {
          this.newMarkdownContent = optimized.content;
          this.$nextTick(() => this.resizeMarkdownEditor("addFileEditor"));
        } else {
          this.markdown = optimized.content;
          this.$nextTick(() => this.resizeMarkdownEditor());
        }

        this.optimizeFinalized = true;

      } catch (err) {
        this.error = err.message || "Could not optimize markdown content.";
      } finally {
        clearInterval(this.optimizingIntervalId);
        this.optimizingIntervalId = null;
        this.optimizingMarkdown = false;
      }
    },

    async selectEnvironment(environment) {
      this.selectedEnvironment = environment;
      this.currentCategory = environment;
      this.environmentMenuOpen = false;
      this.openMarkdownGroup = null;
      this.saveLastCategory(environment);

      await this.loadDashboard();
      await this.loadMarkdownSummaries(false);

      this.autoOpenSingleMarkdownGroup();
    },

    autoOpenSingleMarkdownGroup() {
      const groupNames = Object.keys(this.filteredMarkdownGroups);

      if (groupNames.length === 1) {
        this.openMarkdownGroup = groupNames[0];
      }
    },

    toggleEnvironmentMenu() {
      this.environmentMenuOpen = !this.environmentMenuOpen;
    },

    async startReview(topicId = null, allowBroaderFallback = !topicId) {
      if (!this.aiEnabled) {
        if (topicId) {
          await this.viewTopicHistory(topicId);
        } else if (this.currentMarkdownFileId) {
          await this.previewMarkdownFile(this.currentMarkdownFileId);
          await this.loadTopicsInFile(this.currentMarkdownFileId);
        } else {
          this.showMarkdownSummaries();
        }
        return;
      }

      await this.loadReviewQuestion(
        () => Api.startReview(this.currentMarkdownFileId, this.currentCategory, topicId),
        allowBroaderFallback
      );
    },

    async skipReview() {
      if (!this.review) {
        return;
      }

      const topicId = this.review.topicId;
      await this.loadReviewQuestion(
        () => Api.skipReview(topicId, this.currentMarkdownFileId, this.currentCategory),
        true
      );
    },

    async loadReviewQuestion(fetchQuestion, allowBroaderFallback) {
      // If a file's content is already showing (e.g. previewed before
      // clicking Start Review), don't flash a loading state — or, on
      // mobile, the dashboard — over it. Only show those once we know
      // there's nothing already on screen to preserve.
      const markdownAlreadyShown = !!this.currentMarkdownFileId && !!this.markdown;
      const startingFreshReview = markdownAlreadyShown && !this.review;
      // Next Review after the last due topic in this file (or subcategory) -
      // the next question comes from another file, so don't keep the old one
      // on screen. markdownSummaries is refreshed right after submitAnswer.
      const currentFileSummary = this.markdownSummaries.find(
        file => file.markdownFileId === this.currentMarkdownFileId);
      const fileDone = !!this.result && !!currentFileSummary && currentFileSummary.dueCount === 0;
      const subcategoryDone = !!this.result && this.result.dueInSubcategory === 0;

      this.loading = true;
      this.reviewQuestionLoading = true;
      if (!markdownAlreadyShown || subcategoryDone || fileDone) {
        this.markdownLoading = true;
      }
      this.markdownEditing = false;
      this.mobilePageTitle = "Reviews";
      this.error = null;
      this.success = null;
      this.result = null;
      this.userAnswer = "";
      this.answerSubmitted = false;
      this.isRetry = false;
      this.showAccount = false;
      this.leftPanel = "markdown";
      if (!startingFreshReview) {
        this.mobileView = "review";
      }
      this.feedbackHistory = [];
      this.feedbackHistoryVisible = false;
      this.feedbackHistoryLoadedForTopicId = null;
      this.topicDropdownOpen = false;

      try {
        let data = await fetchQuestion();

        if (data.done && this.currentMarkdownFileId && allowBroaderFallback) {
          this.currentMarkdownFileId = null;
          data = await Api.startReview(null, this.currentCategory);
        }

        if (data.done) {
          this.review = null;
          this.historyTopic = null;
          this.reviewMessage = data.message;
          this.topicsInFile = [];
          // Nothing due in the whole category - show the markdown list
          // instead of the last studied file (mobile stays on the dashboard).
          this.leftPanel = "markdowns";
          await this.loadDashboard();
          return;
        }

        this.review = data;
        this.notDueTopic = null;
        this.currentMarkdownFileId = this.review.markdownFileId;
        this.mobileView = "review";

        if (this.review.forcePractice) {
          // A code challenge (started from the dashboard) is forcePractice
          // too, but the user still has to write and submit the challenge
          // answer - so it starts unsubmitted, unlike a not-due/already-done
          // topic which opens straight into the "Try Again" state. It's a
          // fresh, first-attempt challenge though, not a resubmission, so
          // isRetry stays false (see startCodeChallenge below) - only a
          // not-due/already-reviewed plain topic is forced into isRetry
          // practice here.
          this.answerSubmitted = !this.review.codeChallenge;
          this.isRetry = !this.review.codeChallenge;
        }

        if (this.review.markdownContent) {
          this.markdownLoading = false;
          this.markdown = this.review.markdownContent;
        }

        // Keeps the AI badges correct while reviewing, not just when
        // browsing the file via previewMarkdownFile - also resets them for
        // an ad-hoc topic with no backing file (review.optimized/aiGenerated
        // come back false/null from the backend in that case).
        this.currentMarkdownOptimized = !!this.review.optimized;
        this.currentMarkdownAiGenerated = !!this.review.aiGenerated;
        this.currentMarkdownOriginalContent = this.review.originalContent || null;

        this.$nextTick(() => {
          window.scrollTo(0, 0);
          this.openCurrentTopicCode();
        });

        await this.loadDashboard();

        await this.loadFeedbackHistory(this.review.topicId);
        await this.loadMarkdownReviewCount();
        await this.loadMarkdownCodeChallenges();
        await this.loadTopicsInFile(this.review.markdownFileId);

      } catch (err) {
        this.review = null;
        this.historyTopic = null;
        this.error = err.message || "Could not start review.";
        // Make sure the error is actually visible even if we deferred
        // switching mobileView while hoping for a quiet success above.
        this.mobileView = "review";
      } finally {
        this.loading = false;
        this.markdownLoading = false;
        this.reviewQuestionLoading = false;
      }
    },

    async startReviewForFile(markdownFileId) {
      this.currentMarkdownFileId = markdownFileId;

      // Disable broadening here so a miss never briefly renders a due
      // question from a different file — go straight to this file's own
      // first topic below instead.
      await this.startReview(null, false);

      // Nothing was due in this specific file (and no error, e.g. the
      // daily question limit, stopped us first). Since the user explicitly
      // chose this file, show them its first topic anyway instead of
      // silently redirecting away from it - but don't spend an AI call
      // generating a question nobody asked for yet. When AI is disabled
      // there's no question to fetch anyway, so fall back to the same
      // read-only topic history browsing used elsewhere.
      if (!this.review && !this.error) {
        let topics = [];

        try {
          topics = await Api.loadTopicsByMarkdownFile(markdownFileId);
        } catch (err) {
          topics = [];
        }

        if (topics.length > 0) {
          this.currentMarkdownFileId = markdownFileId;

          if (!this.aiEnabled) {
            await this.viewTopicHistory(topics[0].id);
            return;
          }

          await this.previewMarkdownFile(markdownFileId);
          this.notDueTopic = { topicId: topics[0].id, topicTitle: topics[0].title };
          this.userAnswer = "";
          this.mobilePageTitle = "Reviews";
          this.mobileView = "review";
          await this.loadFeedbackHistory(topics[0].id);
        }
      }
    },

    // Fetches a real practice question for a topic the user was shown as
    // "not due" (see startReviewForFile above) only once they explicitly
    // ask for it, instead of spending an AI call automatically.
    async startPracticeForNotDueTopic() {
      if (!this.notDueTopic || this.loading) {
        return;
      }

      // Leave notDueTopic set (loadReviewQuestion clears it once the real
      // review lands, or leaves it as a fallback on error) so the panel
      // keeps showing instead of falling back to the dashboard mid-fetch.
      const topicId = this.notDueTopic.topicId;
      this.retryMenuOpen = false;
      await this.startReview(topicId);

      // The topic isn't due, so loadReviewQuestion's forcePractice check
      // jumps straight to answerSubmitted (as it should for a topic
      // reopened from the topic dropdown, already answered elsewhere) -
      // but here the user just explicitly chose to practice it for the
      // first time this session, so show Submit Answer like Code
      // Challenge does, not the already-answered Try Again state.
      if (this.review) {
        this.answerSubmitted = false;
      }
    },

    // Shows a file's markdown without generating a review question, so
    // browsing files doesn't spend the daily question limit.
    async previewMarkdownFile(markdownFileId) {
      this.currentMarkdownFileId = markdownFileId;
      this.review = null;
      this.historyTopic = null;
      this.notDueTopic = null;
      this.result = null;
      this.error = null;
      this.success = null;
      this.markdownEditing = false;
      this.viewingCodeChallenges = false;
      this.viewingOriginal = false;
      this.editingOriginalMarkdown = false;
      this.leftPanel = "markdown";
      this.mobilePageTitle = "Markdown";
      this.mobileView = "markdown";
      this.markdownLoading = true;

      try {
        const data = await Api.getMarkdown(markdownFileId);
        this.markdown = data.content;
        this.currentMarkdownOptimized = !!data.optimized;
        this.currentMarkdownAiGenerated = !!data.aiGenerated;
        this.currentMarkdownOriginalContent = data.originalContent || null;
      } catch (err) {
        this.error = err.message || "Could not load markdown file.";
      } finally {
        this.markdownLoading = false;
      }

      await this.loadTopicsInFile(markdownFileId);
      await this.loadMarkdownReviewCount();
      await this.loadMarkdownCodeChallenges();
    },

    // Back/forward buttons at the bottom of the markdown view - restricted
    // to previousMarkdownFileId/nextMarkdownFileId, which only walk files
    // within the current category. An open KB / CC section stays open on the
    // next file when that file has one, so the user can page through them.
    async goToAdjacentMarkdownFile(markdownFileId) {
      if (!markdownFileId) {
        return;
      }

      const keepOriginal = this.viewingOriginal;
      const keepCodeChallenges = this.viewingCodeChallenges;

      await this.previewMarkdownFile(markdownFileId);

      if (this.currentMarkdownFileId !== markdownFileId) {
        return;
      }

      if (keepOriginal && this.currentMarkdownOptimized) {
        this.viewingOriginal = true;
      } else if (keepCodeChallenges && this.currentMarkdownCodeChallenges.length > 0) {
        this.viewingCodeChallenges = true;
      }
    },

    async startReviewFromDashboard() {
      if (this.currentMarkdownFileId) {
        await this.startReviewForFile(this.currentMarkdownFileId);
      } else {
        await this.startReview();
      }
    },

    toggleDashboardActionMenu() {
      this.dashboardActionMenuOpen = !this.dashboardActionMenuOpen;
    },

    // Dashboard "Code Challenge" option: the backend picks the least-
    // challenged topic in the category (in markdown-list order), so the user
    // gets a challenge from a rarely-visited file without opening it. Routed
    // through loadReviewQuestion so the panel transition, markdown load and
    // follow-up fetches match a normal Start Review.
    async startDashboardCodeChallenge() {
      this.dashboardActionMenuOpen = false;
      await this.loadReviewQuestion(
        () => Api.startCategoryCodeChallenge(this.currentCategory),
        false
      );
    },

    async loadTopicsInFile(markdownFileId) {
      if (!markdownFileId) {
        this.topicsInFile = [];
        return;
      }

      try {
        this.topicsInFile = await Api.loadTopicsByMarkdownFile(markdownFileId);
      } catch (err) {
        this.topicsInFile = [];
      }
    },

    openTopicDropdown(event) {
      const iconRect = event.currentTarget.getBoundingClientRect();
      const dropdownWidth = 240; // matches .topic-select-dropdown min-width
      const margin = 16;

      this.topicDropdownAlignRight = iconRect.left + dropdownWidth + margin > window.innerWidth;
      this.topicDropdownOpen = true;
    },

    closeTopicDropdown() {
      this.topicDropdownOpen = false;
    },

    toggleTopicDropdown(event) {
      if (this.topicDropdownOpen) {
        this.closeTopicDropdown();
      } else {
        this.openTopicDropdown(event);
      }
    },

    async selectTopic(topicId) {
      this.topicDropdownOpen = false;

      if (this.activeTopic && topicId === this.activeTopic.topicId) {
        return;
      }

      if (!this.aiEnabled) {
        await this.viewTopicHistory(topicId);
        return;
      }

      const topic = this.topicsInFile.find(t => t.id === topicId);
      const notDueByDate = topic && topic.nextReviewAt && new Date(topic.nextReviewAt) > new Date();

      // A forced-practice review earlier today (e.g. from this same not-due
      // screen) never moves nextReviewAt, so the topic can look "due" again
      // by the time it's revisited even though it was already answered
      // today - mirrors ReviewService.startReview's alreadyReviewedToday
      // half of forcePractice, which nextReviewAt alone doesn't capture.
      await this.loadFeedbackHistory(topicId);
      const reviewedToday = this.feedbackHistory.length > 0
        && new Date(this.feedbackHistory[0].createdAt).toDateString() === new Date().toDateString();

      if (notDueByDate || reviewedToday) {
        await this.previewMarkdownFile(this.currentMarkdownFileId);
        this.notDueTopic = { topicId, topicTitle: topic.title };
        this.userAnswer = "";
        this.mobilePageTitle = "Reviews";
        this.mobileView = "review";
        return;
      }

      await this.startReview(topicId);
    },

    async viewTopicHistory(topicId) {
      const topic = this.topicsInFile.find(t => t.id === topicId);

      this.review = null;
      this.notDueTopic = null;
      this.historyTopic = {
        topicId,
        topicTitle: topic ? topic.title : ""
      };

      this.leftPanel = "markdown";
      this.mobilePageTitle = "Reviews";
      this.mobileView = "review";
      this.feedbackHistoryVisible = true;
      await this.loadFeedbackHistory(topicId);
    },

    async showReviewsHistory() {
      if (!this.currentMarkdownFileId) {
        return;
      }

      // Same as clicking the Score badge - toggle the history panel over
      // whatever topic is already active, instead of clearing review /
      // notDueTopic and hiding the title, textarea, and buttons.
      if (this.activeTopic) {
        this.toggleFeedbackHistory();
        return;
      }

      await this.loadTopicsInFile(this.currentMarkdownFileId);

      if (this.topicsInFile.length > 0) {
        await this.selectTopic(this.topicsInFile[0].id);
        this.feedbackHistoryVisible = true;
      }
    },

    async uploadMarkdown(event) {
      const file = event.target.files[0];

      if (!file) {
        return;
      }

      this.mobileView = "dashboard";
      this.loading = true;
      this.error = null;
      this.success = null;
      this.result = null;

      try {
        this.markdown = await file.text();

        const uploaded = await Api.uploadMarkdown(file);

        this.currentMarkdownFileId = uploaded.markdownFileId;
        this.markdown = uploaded.content;
        this.leftPanel = "markdown";
        this.mobileView = "review"; // show markdown + right panel on mobile

        await this.startReview();
        await this.loadCategoryOptions();
        await this.loadDashboard();
        await this.loadMarkdownSummaries(false);

      } catch (err) {
        this.error = err.message || "Could not upload markdown file.";
      } finally {
        this.loading = false;
        event.target.value = "";
      }
    },

    toggleUploadMenu() {
      this.uploadMenuOpen = !this.uploadMenuOpen;
    },

    triggerSingleFileUpload() {
      this.uploadMenuOpen = false;
      this.$refs.singleFileInput.click();
    },

    triggerVaultUpload() {
      this.uploadMenuOpen = false;
      this.$refs.vaultFolderInput.click();
    },

    // Admin-only. event.target.files is every file under the selected
    // folder (webkitdirectory) - each File carries a webkitRelativePath
    // the backend uses to derive Environment/Category/Subcategory/
    // FileTitle for files that don't already have their own header line.
    // Non-markdown files (images, .obsidian/ config, etc.) are filtered
    // out here rather than sent and skipped server-side.
    async uploadVaultFiles(event) {
      // The File API has no filesystem creation time - lastModified is the
      // closest available proxy, and the folder picker otherwise returns
      // entries alphabetically rather than in any meaningful order.
      const files = Array.from(event.target.files || [])
        .filter(file => /\.md$/i.test(file.name))
        .sort((a, b) => a.lastModified - b.lastModified);

      if (files.length === 0) {
        this.error = "No .md files found in the selected folder.";
        event.target.value = "";
        return;
      }

      this.mobileView = "dashboard";
      this.loading = true;
      this.error = null;
      this.success = null;
      this.result = null;

      try {
        const paths = files.map(file => file.webkitRelativePath || file.name);
        const response = await Api.uploadVault(files, paths);

        this.showMarkdownSummaries();
        this.success = `Imported ${response.imported.length} of ${files.length} file` +
          (files.length === 1 ? "" : "s") + " from the vault.";

        if (response.errors.length > 0) {
          this.error = response.errors.map(e => `${e.path}: ${e.message}`).join("\n");
        }

        await this.loadCategoryOptions();
        await this.loadDashboard();
        await this.loadMarkdownSummaries(false);

      } catch (err) {
        this.error = err.message || "Could not upload the vault.";
      } finally {
        this.loading = false;
        event.target.value = "";
      }
    },

    async submitAnswer() {
      if (this.answerSubmitted || this.loading) {
        return;
      }

      const answerLength = this.userAnswer.trim().length;

      if (answerLength > MAX_ANSWER_LENGTH) {
        this.error = `Your answer is too long (${answerLength}/${MAX_ANSWER_LENGTH} characters) - please shorten it.`;
        return;
      }

      if (this.userAnswer.trim().length === 0) {
        this.error = `Please enter an answer before submitting.`;
        return;
      }

      this.answerSubmitted = true;
      this.loading = true;
      this.error = null;
      this.success = null;

      try {
        this.result = await Api.submitAnswer({
          topicId: this.review.topicId,
          question: this.review.question,
          userAnswer: this.userAnswer,
          isRetry: this.isRetry,
          codeChallenge: this.review.codeChallenge || false
        });

        this.feedbackHistoryVisible = false;

        await this.loadDashboard();
        await this.loadMarkdownSummaries(false);
        await this.loadCategoryOptions();
        await this.loadFeedbackHistory(this.review.topicId);

        // Refresh the Code Challenges list so this attempt's score and
        // "reviewed" timestamp show immediately, whether or not the list
        // is currently open.
        if (this.review.codeChallenge) {
          await this.loadMarkdownCodeChallenges();
        }

        // Retries and code challenges don't move memoryScore, so mastery
        // can only change on a real graded answer.
        if (!this.isRetry) {
          await this.loadStarterPackStatus();
        }

        if (!this.review) {
          this.showMarkdownSummaries();
        }

      } catch (err) {
        this.error = err.message || "Could not submit answer.";
      } finally {
        this.loading = false;
      }
    },

    // "Try Again" (Code Challenge) / "Review Again" (review): keeps the
    // same question in place instead of fetching a new one. A Code
    // Challenge retry clears the answer field for a fresh attempt; a
    // review's "Review Again" keeps the previous answer so it can be
    // tweaked and resubmitted. Marked as a retry so the backend grades it
    // and saves it to history, but it's pure practice: it never moves
    // memoryScore/nextReviewAt/reviewStreak, and (unlike the original,
    // non-retry attempt) never counts toward the streak, goal days, or XP,
    // no matter how well it scores.
    retryQuestion() {
      if (this.review && this.review.codeChallenge) {
        this.userAnswer = "";
      }

      this.answerSubmitted = false;
      this.isRetry = true;
      this.error = null;
      this.success = null;
      this.result = null;
    },

    toggleRetryMenu() {
      this.retryMenuOpen = !this.retryMenuOpen;
    },

    // Fetches a Code Challenge for the same topic just answered: a worded
    // prompt asking the user to write code, graded strictly. This is a
    // fresh, first attempt (isRetry = false) so it's still eligible for
    // goal days / XP if it scores well - the backend never lets it move
    // memoryScore/nextReviewAt/reviewStreak regardless, since that's gated
    // on codeChallenge rather than isRetry. Only resubmitting it via "Try
    // Again" (retryQuestion above) is a retry.
    // topicId is only passed when starting from the "not due" prompt
    // (see startPracticeForNotDueTopic above), where there's no review
    // loaded yet to read a topicId from.
    async startCodeChallenge(topicId) {
      const targetTopicId = topicId || (this.review && this.review.topicId);

      if (!targetTopicId || this.loading) {
        return;
      }

      this.retryMenuOpen = false;
      this.loading = true;
      this.reviewQuestionLoading = true;
      this.codeChallengeLoading = true;
      this.error = null;
      this.success = null;
      this.result = null;

      try {
        this.review = await Api.startCodeChallenge(targetTopicId);
        this.notDueTopic = null;
        this.userAnswer = "";
        this.answerSubmitted = false;
        this.isRetry = false;
      } catch (err) {
        this.error = err.message || "Could not start code challenge.";
      } finally {
        this.loading = false;
        this.reviewQuestionLoading = false;
        this.codeChallengeLoading = false;
      }
    },

    async loadCategoryOptions() {
      try {
        const options = await Api.loadCategoryOptions();
        this.categoryOptions = options || [];
        await this.loadStarterPackStatus();

        const hasCurrentCategory = this.categoryOptions.some(
          option => option.category === this.currentCategory
        );

        if (!hasCurrentCategory && this.categoryOptions.length > 0) {
          const lastCategory = this.readLastCategory();

          const hasLastCategory = lastCategory && this.categoryOptions.some(
            option => option.category === lastCategory
          );

          const fallbackCategory = hasLastCategory
            ? lastCategory
            : this.categoryOptions[0].category;

          this.selectedEnvironment = fallbackCategory;
          this.currentCategory = fallbackCategory;
        }

      } catch (err) {
        console.error("Could not load category options", err);
      }
    },

    // Starter Pack button badges (per-category reviewed %) and the unlock
    // state of dependent categories. Both are computed live on the backend
    // from review history, so re-fetching after every graded answer keeps
    // the badges current without a page refresh. Kept separate from
    // loadCategoryOptions so callers that only need fresh starter-pack
    // numbers don't also trigger the category-options fetch and its
    // fallback-category side effects.
    async loadStarterPackStatus() {
      try {
        this.starterPackUnlockStatus = await Api.loadStarterPackUnlockStatus();
        this.starterPackMastery = await Api.loadStarterPackMastery();
        this.starterPackDue = await Api.loadStarterPackDue();
      } catch (err) {
        console.error("Could not load starter pack status", err);
      }
    },

    // Predefined profiles + the user's saved choice, used to filter the
    // Starter Pack rows. Loaded once; the profile list doesn't change.
    async loadJobProfiles() {
      try {
        const [profiles, active] = await Promise.all([
          Api.loadJobProfiles(),
          Api.loadActiveJobProfile()
        ]);

        this.jobProfiles = profiles;
        this.activeJobProfileId = active ? active.id : null;
      } catch (err) {
        console.error("Could not load job profiles", err);
      }
    },

    async selectJobProfile(value) {
      const previousId = this.activeJobProfileId;
      const profileId = value ? Number(value) : null;

      this.error = null;
      this.activeJobProfileId = profileId;

      try {
        await Api.saveActiveJobProfile(profileId);
      } catch (err) {
        this.activeJobProfileId = previousId;
        this.error = err.message;
      }
    },

    readLastCategory() {
      try {
        return localStorage.getItem(LAST_CATEGORY_STORAGE_KEY);
      } catch (err) {
        return null;
      }
    },

    saveLastCategory(category) {
      try {
        localStorage.setItem(LAST_CATEGORY_STORAGE_KEY, category);
      } catch (err) {
        // localStorage unavailable (disabled/private mode) - preference just won't persist
      }
    },

    async loadDashboard() {
      try {
        const data = await Api.loadDashboard(this.currentCategory);

        if (data) {
          this.dashboard = data;
        }

      } catch (err) {
        console.error("Could not load dashboard", err);
      }
    },

    async loadMarkdownSummaries(autoShow = true) {
      try {
        const data = await Api.loadMarkdownSummaries(this.currentCategory);

        if (!data) {
          return;
        }

        this.markdownSummaries = data;

        if (!autoShow) {
          return;
        }

        if (this.isDesktop) {
          this.showMarkdownSummaries();
        } else {
          this.markdown = "";
          this.leftPanel = "markdown";
          this.mobileView = "dashboard";
          this.mobilePageTitle = "Dashboard";
        }

        if (!this.isDesktop) { // On mobile, don't show the default text
          this.markdown = "";
          this.leftPanel = "markdown";
          this.mobileView = "dashboard";
          this.mobilePageTitle = "Dashboard";
        }

      } catch (err) {
        console.error("Could not load markdown summaries", err);
      }
    },

    async loadCurrentUser() {
      try {
        const data = await Api.loadCurrentUser();

        if (data) {
          this.currentUser = data;
        }

      } catch (err) {
        console.error("Could not load current user", err);
      }
    },

    async goHome() {
      this.error = null;
      this.success = null;
      this.review = null;
      this.historyTopic = null;
      this.notDueTopic = null;
      this.result = null;
      this.userAnswer = "";
      this.currentMarkdownFileId = null;
      this.showAccount = false;
      this.mobileView = "dashboard";
      this.leftExpanded = false;

      await this.loadDashboard();

      if (this.isDesktop) {
        this.showMarkdownSummaries();
      }
    },

    showMarkdownSummaries() {
      this.error = null;
      this.success = null;
      this.mobilePageTitle = "Markdowns";
      this.leftPanel = "markdowns";
      this.mobileView = "markdown";
      this.markdownEditing = false;
    },

    renderTextTable(files) {
      const rows = files.map(file => [
        file.fileTitle,
        String(file.topicCont),
        String(file.dueCount),
        this.formatScore(file.averageMemoryScore)
      ]);

      const headers = ["File", "Topics", "Due today", "Avg memory"];

      const widths = headers.map((header, index) =>
        Math.max(
          header.length,
          ...rows.map(row => row[index].length)
        )
      );

      const line = (left, middle, right) =>
        left + widths.map(width => "─".repeat(width + 2)).join(middle) + right;

      const row = values =>
        "│ " +
        values.map((value, index) => value.padEnd(widths[index])).join(" │ ") +
        " │";

      return [
        line("┌", "┬", "┐"),
        row(headers),
        line("├", "┼", "┤"),
        ...rows.map(row),
        line("└", "┴", "┘")
      ].join("\n");
    },

    async showAccountDetails() {
      this.error = null;
      this.success = null;
      this.mobilePageTitle = "Account Details";
      this.leftPanel = "account";
      this.mobileView = "markdown";
      this.markdownEditing = false;
      await this.loadDashboard();
    },

    async showStarterPack() {
      this.error = null;
      this.success = null;
      this.mobilePageTitle = "Starter Pack";
      this.leftPanel = "starter-pack";
      this.mobileView = "markdown";
      this.markdownEditing = false;
      await this.loadStarterPackStatus();
    },

    async toggleMasteredPanel() {
      if (!this.isDesktop) {
        await this.showStarterPack();
        return;
      }

      if (this.leftPanel === "starter-pack") {
        this.showMarkdownSummaries();
      } else {
        await this.showStarterPack();
      }
    },

    async showDashboard() {
      this.error = null;
      this.success = null;
      this.review = null;
      this.historyTopic = null;
      this.notDueTopic = null;
      this.result = null;
      this.userAnswer = "";
      this.currentMarkdownFileId = null;
      this.mobileView = "dashboard";
      this.leftExpanded = false;
      this.mobilePageTitle = "Dashboard";
      this.markdownEditing = false;

      await this.loadDashboard();

      if (this.isDesktop) {
        this.showMarkdownSummaries();
      }
    },

    closeLeftPanel() {
      this.error = null;
      this.success = null;
      this.showAccount = false;
      this.mobileView = "dashboard";
    },

    stringHash(value) {
      let hash = 0;

      for (let i = 0; i < value.length; i++) {
        hash = value.charCodeAt(i) + ((hash << 5) - hash);
      }

      return Math.abs(hash);
    },

    environmentColor(index) {
      const palette = [
        //"#f45b00", // orange
        //"#8b5cf6", // purple
        "#3b82f6"  // blue
      ];

      return palette[index % palette.length];
    },

    groupHasDue(group) {
      return group.files.some(file => file.dueCount > 0);
    },

    categoryDueColor(category) {
      if (!category.started) {
        return "var(--muted)";
      }

      return category.dueCount > 0 ? "var(--orange)" : "var(--green)";
    },

    softColor(hex) {
      return hex + "18";
    },

    toggleFeedbackHistory() {
      this.feedbackHistoryVisible = !this.feedbackHistoryVisible;
    },

    async loadFeedbackHistory(topicId) {
      this.feedbackHistoryLoading = true;

      try {
        this.feedbackHistory = await Api.loadFeedbackHistory(topicId);
        this.feedbackHistoryLoadedForTopicId = topicId;
      } finally {
        this.feedbackHistoryLoading = false;
      }
    },

    openAdminUsers() {
      this.leftPanel = "admin-users";
      this.mobilePageTitle = "Admin Users";
      this.mobileView = "markdown";
      this.markdownEditing = false;
      this.loadAdminUsers();
    },

    async loadAdminUsers() {
      [this.adminUsers, this.adminVisits] = await Promise.all([
        Api.loadAdminUsers(),
        Api.loadAdminVisits()
      ]);
    },

    showDangerZonePage() {
      this.leftPanel = "danger-zone";
      this.leftExpanded = false;
      this.menuOpen = false;
      this.mobileMenuOpen = false;
      this.mobileView = "markdown";
      this.markdownEditing = false;
    },

    async resetLearningData() {
      const confirmed = confirm(
        "Delete all your learning data? This cannot be undone."
      );

      if (!confirmed) {
        return;
      }

      try {
        this.loading = true;
        this.error = null;
        this.success = null;
        await Api.resetLearningData();
        this.showDangerZone = false;

        this.review = null;
        this.historyTopic = null;
        this.result = null;
        this.markdown = "";
        this.selectedMarkdownFileId = null;

        this.markdownSummaries = [];
        this.categoryOptions = [];
        this.currentCategory = null;
        this.selectedEnvironment = null;
        this.openMarkdownGroup = null;
        this.environmentMenuOpen = false;

        this.markdown = this.initialMarkdown;
        this.leftPanel = "markdowns";
        this.mobileView = "markdown";

        await this.loadCategoryOptions();
        await this.loadDashboard();

        this.success = "Learning data reset successfully.";

      } catch (e) {
        this.error = e.message;
        this.success = null;
      } finally {
        this.loading = false;
      }
    },

    async resetReviewsData() {
      const confirmed = confirm(
        "Delete all your reviews data? This cannot be undone."
      );

      if (!confirmed) {
        return;
      }

      try {
        this.loading = true;
        this.error = null;
        this.success = null;
        await Api.resetReviewsData();
        this.showDangerZone = false;

        this.review = null;
        this.historyTopic = null;
        this.result = null;
        this.markdown = "";
        this.selectedMarkdownFileId = null;

        this.markdownSummaries = [];
        this.categoryOptions = [];
        this.currentCategory = null;
        this.selectedEnvironment = null;
        this.openMarkdownGroup = null;
        this.environmentMenuOpen = false;

        this.markdown = this.initialMarkdown;
        this.leftPanel = "markdowns";
        this.mobileView = "markdown";

        await this.loadCategoryOptions();
        await this.loadDashboard();

        this.success = "Reviews reset successfully.";

      } catch (e) {
        this.error = e.message;
        this.success = null;
      } finally {
        this.loading = false;
      }
    },

    async resetFileReviews() {
      if (!this.currentMarkdownFileId || this.currentMarkdownReviewCount === 0) {
        return;
      }

      const confirmed = confirm(
        "Delete all reviews and progress for this markdown file? This cannot be undone."
      );

      if (!confirmed) {
        return;
      }

      try {
        this.loading = true;
        this.error = null;
        this.success = null;

        await Api.resetMarkdownReviews(this.currentMarkdownFileId);

        this.review = null;
        this.historyTopic = null;
        this.result = null;
        this.feedbackHistory = [];
        this.feedbackHistoryVisible = false;
        this.feedbackHistoryLoadedForTopicId = null;

        await this.loadMarkdownSummaries(false);
        await this.loadDashboard();
        await this.loadMarkdownReviewCount();
        await this.loadMarkdownCodeChallenges();
        this.viewingCodeChallenges = false;

        this.success = "Reviews reset for this file.";

      } catch (e) {
        this.error = e.message;
        this.success = null;
      } finally {
        this.loading = false;
      }
    },

    async loadMarkdownReviewCount() {
      if (!this.currentMarkdownFileId) {
        this.currentMarkdownReviewCount = null;
        return;
      }

      this.currentMarkdownReviewCount = null;

      try {
        this.currentMarkdownReviewCount = await Api.getMarkdownReviewCount(this.currentMarkdownFileId);
      } catch (e) {
        this.currentMarkdownReviewCount = null;
      }
    },

    async loadMarkdownCodeChallenges() {
      if (!this.currentMarkdownFileId) {
        this.currentMarkdownCodeChallenges = [];
        return;
      }

      this.currentMarkdownCodeChallenges = [];

      try {
        this.currentMarkdownCodeChallenges = await Api.getMarkdownCodeChallenges(this.currentMarkdownFileId);
      } catch (e) {
        this.currentMarkdownCodeChallenges = [];
      }
    },

    // "n days ago" label for a challenge's reviewedAt timestamp.
    daysAgoLabel(dateString) {
      const days = Math.floor((Date.now() - new Date(dateString).getTime()) / 86400000);

      if (days <= 0) return "Today";
      if (days === 1) return "1 day ago";
      return days + " days ago";
    },

    // Loads one past code challenge from the list back into the review
    // pane so it can be retried from within the list itself: same topic
    // and question, with a blank answer field for a fresh attempt. Marked
    // as a retry (isRetry = true), so it's pure practice - graded and
    // saved, but never counts toward the streak, goal days, or XP.
    tryAgainFromList(c) {
      if (this.loading) {
        return;
      }

      this.review = {
        topicId: c.topicId,
        topicTitle: c.topicTitle,
        question: c.question,
        markdownContent: this.markdown,
        markdownFileId: this.currentMarkdownFileId,
        done: false,
        message: null,
        forcePractice: true,
        codeChallenge: true
      };

      this.notDueTopic = null;
      this.userAnswer = "";
      this.answerSubmitted = false;
      this.isRetry = true;
      this.error = null;
      this.success = null;
      this.result = null;
      this.viewingCodeChallenges = false;
      this.mobileView = "review";

      this.$nextTick(() => window.scrollTo(0, 0));
    },

    showCodeChallenges() {
      if (!this.currentMarkdownFileId || this.currentMarkdownCodeChallenges.length === 0) {
        return;
      }

      this.markdownEditing = false;
      this.leftPanel = "markdown";
      this.viewingCodeChallenges = true;
      this.viewingOriginal = false;
      this.editingOriginalMarkdown = false;
    },

    hideCodeChallenges() {
      this.viewingCodeChallenges = false;
    },

    // CC button / menu - same section-under-the-markdown toggle as KB.
    toggleCodeChallenges() {
      if (this.viewingCodeChallenges) {
        this.hideCodeChallenges();
      } else {
        this.showCodeChallenges();
      }
    },

    async deleteMarkdownFile() {
      if (!this.currentMarkdownFileId) {
        return;
      }

      if (this.currentMarkdownReviewCount !== 0) {
        this.error = "This file has reviews — reset them before deleting it.";
        this.success = null;
        return;
      }

      const confirmed = confirm(
        "Delete this markdown file and all its topics? This cannot be undone."
      );

      if (!confirmed) {
        return;
      }

      try {
        this.loading = true;
        this.error = null;
        this.success = null;

        await Api.deleteMarkdown(this.currentMarkdownFileId);

        this.currentMarkdownFileId = null;
        this.currentMarkdownReviewCount = null;
        this.currentMarkdownCodeChallenges = [];
        this.viewingCodeChallenges = false;
        this.review = null;
        this.historyTopic = null;
        this.result = null;
        this.markdown = "";
        this.feedbackHistory = [];
        this.feedbackHistoryVisible = false;
        this.feedbackHistoryLoadedForTopicId = null;

        this.leftPanel = "markdowns";
        this.mobileView = "markdown";

        await this.loadCategoryOptions();
        await this.loadMarkdownSummaries(false);
        await this.loadDashboard();

        this.success = "Markdown file deleted.";

      } catch (e) {
        this.error = e.message;
        this.success = null;
      } finally {
        this.loading = false;
      }
    },

    async importStarterPack(category) {
      try {
        this.loading = true;
        this.error = null;
        this.success = null;

        await Api.importStarterPack(category);

        await this.loadCategoryOptions();

        this.currentCategory = category;
        this.selectedEnvironment = category;

        await this.loadDashboard();
        await this.loadMarkdownSummaries(true);

        this.autoOpenSingleMarkdownGroup();

      } catch (e) {
        this.error = e.message;
      } finally {
        this.loading = false;
      }
    },

    async openImportedCategory(category) {
      this.error = null;
      this.success = null;
      this.leftPanel = "markdowns";
      this.markdownEditing = false;

      if (this.isDesktop) {
        this.mobilePageTitle = "Markdowns";
        this.mobileView = "markdown";
      } else {
        this.mobilePageTitle = "Dashboard";
        this.mobileView = "dashboard";
      }

      await this.selectEnvironment(category);
    },

    showMarkdownExample() {
      this.error = null;
      this.success = null;
      this.mobilePageTitle = "Markdown Example";
      this.leftPanel = "markdown";
      this.showAccount = false;
      this.mobileView = "markdown";
      this.currentMarkdownFileId = null;
      this.markdownEditing = false;
      this.markdown = `
<h3>File Example Pattern</h3>

<pre># Environment / Category / Subcategory / File Title

## 1. Topic
Content...

## 2. Topic
Content...</pre>

<h3>Example: threads_theory.md</h3>

<pre># JVM / Java / Threads / Theory

## 1. Creating Threads
Thread creation basics...

## 2. Implementing Runnable
Java supports single inheritance...</pre>
`;
    },

    showAddFile() {
      this.error = null;
      this.success = null;
      this.mobilePageTitle = "Add File";
      this.leftPanel = "add-file";
      this.mobileView = "markdown";
      this.newMarkdownContent = "";
      this.optimizeFinalized = false;
      this.pendingOptimization = null;
    },

    async submitAddFile() {
      if (!this.newMarkdownContent.trim()) {
        return;
      }

      this.loading = true;
      this.error = null;
      this.success = null;
      this.result = null;

      const appliedOptimization =
        this.optimizeFinalized && this.pendingOptimization ? this.pendingOptimization.kind : null;
      const originalContentSnapshot =
        appliedOptimization === "STRUCTURED" ? this.pendingOptimization.originalContent : null;

      try {
        const created = await Api.createMarkdown(
          this.newMarkdownContent, appliedOptimization, originalContentSnapshot);

        this.currentMarkdownFileId = created.markdownFileId;
        this.markdown = created.content;
        this.currentMarkdownOptimized = !!created.optimized;
        this.currentMarkdownAiGenerated = !!created.aiGenerated;
        this.currentMarkdownOriginalContent = created.originalContent || null;
        this.pendingOptimization = null;
        this.newMarkdownContent = "";
        this.leftPanel = "markdown";
        this.mobileView = "review"; // show markdown + right panel on mobile

        await this.startReview();
        await this.loadCategoryOptions();
        await this.loadDashboard();
        await this.loadMarkdownSummaries(false);

      } catch (err) {
        this.error = err.message || "Could not create markdown file.";
      } finally {
        this.loading = false;
      }
    }
  }
});

// Reusable icon: <icon name="pencil" class="menu-icon"></icon>
// Backed by the Bootstrap Icons path data in js/icons.js - add a name there
// to use it here or in any future part of the app.
app.component("icon", {
  props: {
    name: { type: String, required: true }
  },
  template: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
    viewBox="0 0 16 16" v-html="ICONS[name] || ''"></svg>`,
  data() {
    return { ICONS };
  }
});

// Import/open/lock behavior shared by every Starter Pack category control
// (<starter-pack-list> buttons and <job-profile-progress> rows).
const starterPackCategoryMixin = {
  props: {
    categoryOptions: { type: Array, default: () => [] },
    unlockStatus: { type: Object, default: () => ({}) },
    mastery: { type: Object, default: () => ({}) }
  },
  emits: ["import", "open"],
  methods: {
    isImported(category) {
      return this.categoryOptions.some(o => o.category === category);
    },
    isLocked(category) {
      return this.unlockStatus[category] === false && !(this.mastery[category] > 0);
    },
    tooltip(category) {
      if (this.isLocked(category)) {
        return "Category locked";
      }

      return this.isImported(category) ? "Go to category" : "Import category";
    },
    onClick(category) {
      this.$emit(this.isImported(category) ? "open" : "import", category);
    },
    dueColorClass(category) {
      const option = this.categoryOptions.find(o => o.category === category);

      if (!option || !option.started) {
        return "";
      }

      return option.dueCount > 0 ? "starter-pack-btn-due" : "starter-pack-btn-caught-up";
    }
  }
};

// Reusable Starter Pack row: <starter-pack-list :categories="['Java', 'Spring']"
//   :category-options="categoryOptions" :unlock-status="starterPackUnlockStatus"
//   :mastery="starterPackMastery" :profile-categories="activeProfileCategories"
//   @import="importStarterPack" @open="openImportedCategory">
// Owns the per-button disabled/badge/tooltip logic so every mounting point (the
// empty-state welcome screen, the standalone Starter Pack page) shares one copy
// instead of duplicating a button block per category.
app.component("starter-pack-list", {
  mixins: [starterPackCategoryMixin],
  props: {
    categories: { type: Array, required: true },
    // Active job profile's category names; null shows every category.
    profileCategories: { type: Array, default: null }
  },
  computed: {
    visibleCategories() {
      if (!this.profileCategories) {
        return this.categories;
      }

      return this.categories.filter(c => this.profileCategories.includes(c));
    }
  },
  template: `
    <div v-if="visibleCategories.length > 0" class="starter-pack-list">
      <button v-for="category in visibleCategories" :key="category" class="starter-pack-btn"
        :class="dueColorClass(category)"
        :disabled="isLocked(category)" :title="tooltip(category)" @click="onClick(category)">
        {{ category }}
        <span v-if="mastery[category] > 0" class="starter-pack-mastery-badge">{{ mastery[category] || 0 }}%</span>
      </button>
    </div>
  `
});

// Starter Pack view for an active job profile: <job-profile-progress
//   :profile-categories="activeProfileCategories" :category-options="categoryOptions"
//   :unlock-status="starterPackUnlockStatus" :mastery="starterPackMastery"
//   :due="starterPackDue" @import="importStarterPack" @open="openImportedCategory">
// One bar per profile category that is a starter pack, in the same order as
// the Starter Pack button rows (STARTER_PACK_ROWS): reviewed % in green,
// followed by due % (reviewed before, due again) in yellow.
app.component("job-profile-progress", {
  mixins: [starterPackCategoryMixin],
  props: {
    profileCategories: { type: Array, required: true },
    due: { type: Object, default: () => ({}) }
  },
  computed: {
    visibleCategories() {
      return STARTER_PACK_ROWS.flat().filter(c => this.profileCategories.includes(c));
    }
  },
  methods: {
    percent(category) {
      return this.mastery[category] || 0;
    },
    // Capped so independently rounded reviewed + due never overflow the track.
    duePercent(category) {
      return Math.min(this.due[category] || 0, 100 - this.percent(category));
    }
  },
  template: `
    <div v-if="visibleCategories.length > 0" class="job-profile-progress">
      <button v-for="category in visibleCategories" :key="category" class="job-profile-row"
        :disabled="isLocked(category)" :title="tooltip(category)" @click="onClick(category)">
        <span class="job-profile-stack">
          <span class="job-profile-label" :class="dueColorClass(category)">{{ category }}</span>
          <span class="job-profile-track">
            <span class="job-profile-bar" :style="{ width: percent(category) + '%' }"></span>
            <span v-if="duePercent(category) > 0" class="job-profile-bar-due"
              :style="{ width: duePercent(category) + '%' }"
              :title="duePercent(category) + '% due'"></span>
          </span>
        </span>
        <span class="job-profile-percent">{{ percent(category) }}%</span>
      </button>
    </div>
  `
});

app.mount("#app");
