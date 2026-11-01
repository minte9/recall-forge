// Mastery ring: circular memory-score badge with the Leitner box number.
// Registered in app.js as <mastery-ring>.
//
// Parent contract:
//   :score   - topic memory score (0..1)
//   :streak  - topic review streak; the box number shows from 1 up
//   @click   - ring clicked (the app toggles feedback history)
const MasteryRing = {
  props: {
    score: { type: Number, required: true },
    streak: { type: Number, default: 0 }
  },

  emits: ["click"],

  computed: {
    // Same as the app's formatScore.
    label() {
      return Number(this.score).toFixed(2);
    },

    // Rounds the same way the label displays before comparing, so a score
    // like 0.696 (displayed "0.70") is colored high-score instead of low.
    // Same as the app's isHighScore.
    highScore() {
      return Number(Number(this.score).toFixed(2)) >= 0.7;
    },

    // Dash offset for the progress circle - r=23 matches the <circle>
    // radius below, so this stays in sync with .mastery-ring-progress's
    // stroke-dasharray in app.css.
    offset() {
      const circumference = 2 * Math.PI * 23;
      const clamped = Math.max(0, Math.min(1, Number(this.score) || 0));
      return circumference * (1 - clamped);
    }
  },

  template: `
    <span class="mastery-ring mastery-ring--actions"
      :class="highScore ? 'mastery-ring--high-score' : 'mastery-ring--low-score'"
      title="Memory Score - Show feedback history" @click="$emit('click')">
      <svg viewBox="0 0 56 56" class="mastery-ring-svg" aria-hidden="true">
        <circle class="mastery-ring-track" cx="28" cy="28" r="23"></circle>
        <circle class="mastery-ring-progress" cx="28" cy="28" r="23"
          :style="{ strokeDashoffset: offset }"></circle>
      </svg>
      <span class="mastery-ring-label">{{ label }}</span>
      <span v-if="streak >= 1" class="mastery-ring-streak"
        :title="\`Box \${streak + 1}\`">{{ streak + 1 }}</span>
    </span>
  `
};
