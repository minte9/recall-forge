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

  async updateMarkdown(markdownFileId, content) {
    const response = await fetch("/api/markdown/" + encodeURIComponent(markdownFileId), {
      method: "PUT",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ content })
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "Could not save markdown file.");
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
      credentials: "same-origin"
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
};
