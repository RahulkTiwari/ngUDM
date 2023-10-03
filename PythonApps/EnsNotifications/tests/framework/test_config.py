# Third party libs
import unittest
# Custom libs
from modules.config import get_all_configurations


class TestConfig(unittest.TestCase):
    def test_get_configuration(self):
        function_result = get_all_configurations()
        self.assertEqual(['LOGGING', 'SHAREPOINT', 'CONNECTION', 'SERVICE_DESK', 'PROCESSING'], function_result.sections())
        logging_section = function_result['LOGGING']
        self.assertEqual(3, len(logging_section))
        self.assertEqual('DEBUG', logging_section['level'])
        self.assertEqual('ens_notices', logging_section['root_filename'])


if __name__ == '__main__':
    unittest.main()
