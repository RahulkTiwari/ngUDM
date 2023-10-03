# Third party libs
import unittest
# Custom libs
from exchange_parsers.tfex import get_ref_id


class Testtfex(unittest.TestCase):
    def test_get_ref_id(self):
        html_string = '/tfex/newsDetail.html?newsId=16732206150380'
        funtion_result = get_ref_id(html_string)
        self.assertEqual('16732206150380', funtion_result)


if __name__ == '__main__':
    unittest.main()
