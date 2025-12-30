module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    fontFamily: {
      inter: ["Inter", "sans-serif"],
    },
    extend: {
      colors: {
        neutral: {
          50: "#f8fafc",
          100: "#f1f5f9",
          700: "#334155",
          800: "#1e293b"
        },
        slate: {
          500: "#64748b"
        },
        blue: {
          100: "#e0f2fe",
          700: "#1d4ed8"
        },
        green: {
          100: "#d1fae5",
          700: "#047857"
        }
      }
    },
  },
  plugins: [],
};
