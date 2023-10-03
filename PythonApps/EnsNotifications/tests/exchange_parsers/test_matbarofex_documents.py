# Third party libs
import unittest
# Custom libs
from exchange_parsers.matbarofex_documents import prep_subject, get_exchange_reference_id


class TestMatbaforexDocuments(unittest.TestCase):
    def test_prep_subject(self):
        subject_string = '11/01/2023\n\n\n\n\n   - Notice No. 806 - Release of Future Price Limits Grupo Financial Galicia (Ggal)\n    \n\n        '
        function_result = prep_subject(subject_string)
        self.assertEqual('Notice No. 806 - Release of Future Price Limits Grupo Financial Galicia (Ggal)', function_result)

    def test_get_exchange_reference_id(self):
        href_string = '/en/index.php/documentos/legales/circular-nro-806'
        function_result = get_exchange_reference_id(href_string)
        self.assertEqual('806', function_result)


if __name__ == '__main__':
    unittest.main()
