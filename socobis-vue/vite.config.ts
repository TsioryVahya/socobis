import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(),
  ],
  server: {
    proxy: {
      // Proxy all requests starting with /...Servlet to the Java backend
      '^/.*Servlet': {
        target: 'http://localhost:8080/socobis/',
        changeOrigin: true,
        configure: (proxy, _options) => {
          proxy.on('proxyRes', (proxyRes, req, res) => {
            const sc = proxyRes.headers['set-cookie'];
            if (sc) {
              proxyRes.headers['set-cookie'] = sc.map(s => 
                s.replace(/Path=\/socobis/i, 'Path=/')
              );
            }
          });
        },
      },
      '/api': {
        target: 'http://localhost:8080/socobis/',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
        configure: (proxy, _options) => {
          proxy.on('proxyRes', (proxyRes, req, res) => {
            const sc = proxyRes.headers['set-cookie'];
            if (sc) {
              proxyRes.headers['set-cookie'] = sc.map(s => 
                s.replace(/Path=\/socobis/i, 'Path=/')
              );
            }
          });
        },
      },
    },
  },
})
