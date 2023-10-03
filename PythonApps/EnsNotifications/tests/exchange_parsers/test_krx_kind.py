# Third party libs
import unittest
from bs4 import BeautifulSoup
# Custom libs
from exchange_parsers.krx_kind import get_doc_id


class TestKrxKind(unittest.TestCase):
    def test_get_doc_id(self):
        html_string = '<td><a href="#viewer" onclick="openDisclsViewer(\'20230109000453\',\'\')" title="foobar"><font color="#FF8040">foobar' \
                      '</fontfoobar</a></td>'
        html_soup = BeautifulSoup(html_string, 'lxml')
        function_result = get_doc_id(html_soup)
        self.assertEqual('20230109000453', function_result)


if __name__ == '__main__':
    unittest.main()
