const Api = {
  async startReview(markdownFileId, category, topicId) {
    const params = new URLSearchParams();

    if (markdownFileId) {
      params.set("markdownFileId", markdownFileId);
    }

    if (topicId) {
      params.set("topicId", topicId);
    }

    params.set("category", category || "Java");

    const url = "/api/reviews/start?" + params.toString();

    const response = await fetch(url, {
      method: "POST",
      credentials: "same-origin"
    });

    if (response.redirected || response.url.includes("/login")) {
      window.location.href = "/login";
      return;
    }

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not start review.");
    }

    return await response.json();
  },

  async skipReview(topicId, markdownFileId, category) {
    const params = new URLSearchParams();

    if (markdownFileId) {
      params.set("markdownFileId", markdownFileId);
    }

    params.set("category", category || "Java");

    const url = `/api/reviews/${topicId}/skip?` + params.toString();

    const response = await fetch(url, {
      method: "POST",
      credentials: "same-origin"
    });

    if (response.redirected || response.url.includes("/login")) {
      window.location.href = "/login";
      return;
    }

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not skip this topic.");
    }

    return await response.json();
  },

  async startCodeChallenge(topicId) {
    const response = await fetch(`/api/reviews/${topicId}/code-challenge`, {
      method: "POST",
      credentials: "same-origin"
    });

    if (response.redirected || response.url.includes("/login")) {
      window.location.href = "/login";
      return;
    }

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not start code challenge.");
    }

    return await response.json();
  },

  async startCategoryCodeChallenge(category) {
    const params = new URLSearchParams();
    params.set("category", category || "Java");

    const response = await fetch("/api/reviews/code-challenge?" + params.toString(), {
      method: "POST",
      credentials: "same-origin"
    });

    if (response.redirected || response.url.includes("/login")) {
      window.location.href = "/login";
      return;
    }

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not start code challenge.");
    }

    return await response.json();
  },

  async uploadMarkdown(file) {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch("/api/markdown/upload", {
      method: "POST",
      body: formData,
      credentials: "same-origin"
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not upload markdown file.");
    }

    return await response.json();
  },

  // files[i] must correspond to paths[i] - the file's path relative to the
  // vault root, used by the backend to derive Environment/Category/
  // Subcategory/FileTitle for files that don't already have their own
  // header line.
  async uploadVault(files, paths) {
    const formData = new FormData();
    files.forEach(file => formData.append("files", file));
    paths.forEach(path => formData.append("paths", path));

    const response = await fetch("/api/markdown/upload-vault", {
      method: "POST",
      body: formData,
      credentials: "same-origin"
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not upload the vault.");
    }

    return await response.json();
  },

  async createMarkdown(content, appliedOptimization = null, originalContent = null) {
    const response = await fetch("/api/markdown", {
      method: "POST",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ content, appliedOptimization, originalContent })
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not create markdown file.");
    }

    return await response.json();
  },

  async getMarkdown(markdownFileId) {
    const response = await fetch("/api/markdown/" + encodeURIComponent(markdownFileId), {
      credentials: "same-origin"
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not load markdown file.");
    }

    return await response.json();
  },

  async updateMarkdown(markdownFileId, content, appliedOptimization = null, originalContent = null) {
    const response = await fetch("/api/markdown/" + encodeURIComponent(markdownFileId), {
      method: "PUT",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ content, appliedOptimization, originalContent })
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not save markdown file.");
    }

    return await response.json();
  },

  async updateOriginalMarkdown(markdownFileId, content) {
    const response = await fetch(
      "/api/markdown/" + encodeURIComponent(markdownFileId) + "/original", {
        method: "PUT",
        credentials: "same-origin",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ content })
      });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not save the original markdown.");
    }

    return await response.json();
  },

  async deleteOriginalMarkdown(markdownFileId) {
    const response = await fetch(
      "/api/markdown/" + encodeURIComponent(markdownFileId) + "/original", {
        method: "DELETE",
        credentials: "same-origin"
      });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not delete the original markdown.");
    }

    return await response.json();
  },

  async optimizeMarkdown(content) {
    const response = await fetch("/api/markdown/optimize", {
      method: "POST",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ content })
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not optimize markdown content.");
    }

    return await response.json();
  },

  async resetMarkdownReviews(markdownFileId) {
    const response = await fetch("/api/markdown/" + encodeURIComponent(markdownFileId) + "/reviews", {
      method: "DELETE",
      credentials: "same-origin"
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not reset reviews for this markdown file.");
    }
  },

  async getMarkdownReviewCount(markdownFileId) {
    const response = await fetch("/api/markdown/" + encodeURIComponent(markdownFileId) + "/reviews/count", {
      credentials: "same-origin"
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not load review count for this markdown file.");
    }

    const data = await response.json();
    return data.count;
  },

  async getMarkdownCodeChallenges(markdownFileId) {
    const response = await fetch(
      "/api/markdown/" + encodeURIComponent(markdownFileId) + "/reviews/code-challenges",
      { credentials: "same-origin" }
    );

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not load code challenges for this markdown file.");
    }

    return response.json();
  },

  async deleteMarkdown(markdownFileId) {
    const response = await fetch("/api/markdown/" + encodeURIComponent(markdownFileId), {
      method: "DELETE",
      credentials: "same-origin"
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not delete this markdown file.");
    }
  },

  async submitAnswer(payload) {
    const response = await fetch("/api/reviews/answer", {
      method: "POST",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not submit answer.");
    }

    return await response.json();
  },

  async loadDashboard(category) {
    const url = "/api/dashboard?category=" + encodeURIComponent(category || "Java");

    const response = await fetch(url, {
      credentials: "same-origin"
    });

    if (!response.ok) {
      return null;
    }

    return await response.json();
  },

  async loadMarkdownSummaries(category) {
    const url = "/api/dashboard/markdowns?category=" + encodeURIComponent(category || "Java");

    const response = await fetch(url, {
      credentials: "same-origin"
    });

    if (!response.ok) {
      return null;
    }

    return await response.json();
  },

  async loadCategoryOptions() {
    const response = await fetch("/api/dashboard/categories", {
      credentials: "same-origin",
      cache: "no-store"
    });

    if (!response.ok) {
      return [];
    }

    return await response.json();
  },

  async loadCurrentUser() {
    const response = await fetch("/api/me", {
      credentials: "same-origin"
    });

    if (!response.ok) {
      return null;
    }

    return await response.json();
  },

  async loadFeedbackHistory(topicId) {
    const response = await fetch(`/api/reviews/${topicId}/feedback-history`);

    if (!response.ok) {
      throw new Error("Failed to load feedback history");
    }

    return await response.json();
  },

  async loadTopicsByMarkdownFile(markdownFileId) {
    const response = await fetch(`/api/topics/by-markdown-file/${markdownFileId}`, {
      credentials: "same-origin"
    });

    if (!response.ok) {
      throw new Error("Failed to load topics for this markdown file");
    }

    return await response.json();
  },

  async loadAdminUsers() {
    const response = await fetch("/api/admin/users");

    if (!response.ok) {
      throw new Error("Failed to load admin users");
    }

    return await response.json();
  },

  async loadAdminVisits() {
    const response = await fetch("/api/admin/visits");

    if (!response.ok) {
      throw new Error("Failed to load admin visits");
    }

    return await response.json();
  },

  async loadAdminReferrers() {
    const response = await fetch("/api/admin/referrers");

    if (!response.ok) {
      throw new Error("Failed to load admin referrers");
    }

    return await response.json();
  },

  async resetLearningData() {
    const response = await fetch("/api/me/learning-data", {
      method: "DELETE",
      credentials: "same-origin"
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not reset learning data.");
    }
  },

  async resetReviewsData() {
    const response = await fetch("/api/me/reviews-data", {
      method: "DELETE",
      credentials: "same-origin"
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not reset learning data.");
    }
  },

  async importStarterPack(category) {
    const response = await fetch(
      "/api/starter-packs/" + encodeURIComponent(category) + "/import",
      {
        method: "POST",
        credentials: "same-origin"
      }
    );

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not import starter pack.");
    }
  },

  async loadStarterPackUnlockStatus() {
    const response = await fetch("/api/starter-packs/unlock-status", {
      credentials: "same-origin",
      cache: "no-store"
    });

    if (!response.ok) {
      return {};
    }

    return await response.json();
  },

  async loadStarterPackMastery() {
    const response = await fetch("/api/starter-packs/mastery", {
      credentials: "same-origin",
      cache: "no-store"
    });

    if (!response.ok) {
      return {};
    }

    return await response.json();
  },

  async loadStarterPackDue() {
    const response = await fetch("/api/starter-packs/due", {
      credentials: "same-origin",
      cache: "no-store"
    });

    if (!response.ok) {
      return {};
    }

    return await response.json();
  },

  async loadJobProfiles() {
    const response = await fetch("/api/job-profiles", {
      credentials: "same-origin",
      cache: "no-store"
    });

    if (!response.ok) {
      return [];
    }

    return await response.json();
  },

  // The backend answers 200 with an empty body when no profile is
  // selected ("All Categories"), so parse the text instead of json().
  async loadActiveJobProfile() {
    const response = await fetch("/api/job-profiles/active", {
      credentials: "same-origin",
      cache: "no-store"
    });

    if (!response.ok) {
      return null;
    }

    const text = await response.text();

    return text ? JSON.parse(text) : null;
  },

  async saveActiveJobProfile(profileId) {
    const response = await fetch("/api/job-profiles/active", {
      method: "PUT",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ profileId })
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not save job profile.");
    }
  },
};
