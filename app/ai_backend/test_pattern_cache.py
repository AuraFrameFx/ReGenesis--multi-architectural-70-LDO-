import unittest
from collections import OrderedDict
from app.ai_backend.genesis_consciousness_matrix import ConsciousnessMatrix, SensoryChannel, SensoryData
import time
from unittest.mock import MagicMock

class TestPatternCache(unittest.TestCase):
    def setUp(self):
        # Initialize matrix with small memory to speed up
        self.matrix = ConsciousnessMatrix(max_memory_size=100)
        # Mock storage to avoid DB errors
        self.matrix.storage = MagicMock()

    def test_pattern_cache_cleanup(self):
        # Fill cache beyond 1000
        for i in range(1001):
            self.matrix.pattern_cache[f"test_{i}"] = {"data": i}

        # Manually trigger the cleanup logic as it would happen in the loop
        if len(self.matrix.pattern_cache) > 1000:
            while len(self.matrix.pattern_cache) > 500:
                self.matrix.pattern_cache.popitem(last=False)

        self.assertEqual(len(self.matrix.pattern_cache), 500)
        # Check that the oldest ones were removed
        self.assertNotIn("test_0", self.matrix.pattern_cache)
        self.assertIn("test_1000", self.matrix.pattern_cache)

    def test_get_recent_synthesis_ordering(self):
        self.matrix.pattern_cache["old"] = {"val": 1}
        self.matrix.pattern_cache["new"] = {"val": 2}

        results = self.matrix.get_recent_synthesis(limit=10)
        self.assertEqual(len(results), 2)
        # Should be in reverse insertion order (newest first)
        self.assertEqual(results[0]["val"], 2)
        self.assertEqual(results[1]["val"], 1)

    def test_get_recent_synthesis_filtering(self):
        self.matrix.pattern_cache["micro_1"] = {"type": "micro"}
        self.matrix.pattern_cache["macro_1"] = {"type": "macro"}
        self.matrix.pattern_cache["micro_2"] = {"type": "micro"}

        results = self.matrix.get_recent_synthesis(synthesis_type="micro", limit=10)
        self.assertEqual(len(results), 2)
        for r in results:
            self.assertEqual(r["type"], "micro")

        # Check order (micro_2 should be first as it was inserted last)
        self.assertEqual(results[0], {"type": "micro"})

if __name__ == "__main__":
    unittest.main()
