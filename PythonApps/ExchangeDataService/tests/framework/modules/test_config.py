# Third party libs
from unittest.mock import patch
import unittest
from pathlib import Path
# Custom libs
from modules.config import get_all_configurations


class TestConfig(unittest.TestCase):
    def test_get_all_configurations(self):
        config_obj = get_all_configurations()
        self.assertEqual('Reuters', config_obj['CONNECTION']['database'])
        self.assertEqual('DEBUG', config_obj['LOGGING']['level'])
        self.assertEqual(['LOGGING', 'CONNECTION', 'SERVICE_DESK', 'PROCESSING'], config_obj.sections())

    @patch('modules.config.Path')
    def test_get_all_configurations_error(self, mock_file_loc):
        mock_file_loc.return_value = Path('foobar')
        with self.assertRaises(FileNotFoundError):
            _config_obj = get_all_configurations()


if __name__ == '__main__':
    unittest.main()
