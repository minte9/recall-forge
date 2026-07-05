const Api = {
  async startReview(markdownFileId) {
    let url = "/api/reviews/start";

    if (markdownFileId) {
      url += "?markdownFileId=" + encodeURIComponent(markdownFileId);
    }

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

  async loadAdminUsers() {
    const response = await fetch("/api/admin/users");

    if (!response.ok) {
      throw new Error("Failed to load admin users");
    }

    return await response.json();
  }
};
