# Third party libs
import unittest
from unittest.mock import patch
import pandas as pd
import os
# Custom libs
from modules.regex import get_regex


class TestRegex(unittest.TestCase):

    mock_config_obj = {
        'SHAREPOINT': {
            'download_location': os.path.realpath(os.curdir) + '/tests/resources'
        }
    }

    @patch('modules.regex.config_object', mock_config_obj)
    def test_get_regex(self):
        function_result = get_regex()
        self.assertEqual(48, len(function_result['overview']))
        self.assertIn('overview', function_result.keys())
        self.assertIsInstance(function_result['overview'], pd.DataFrame)


if __name__ == '__main__':
    unittest.main()
