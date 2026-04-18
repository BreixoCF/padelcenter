import { defineConfig } from 'orval';

export default defineConfig({
  padelcenter: {
    input: {
      target: 'http://localhost:8080/api-docs',
    },
    output: {
      mode: 'tags-split',
      target: 'src/lib/api/generated',
      schemas: 'src/lib/api/generated/models',
      client: 'react-query',
      httpClient: 'axios',
      override: {
        mutator: {
          path: 'src/lib/api/client.ts',
          name: 'apiClient',
        },
        query: {
          useQuery: true,
          useMutation: true,
        },
      },
    },
  },
});
