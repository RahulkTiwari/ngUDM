# Third party libs
import unittest
from bs4 import BeautifulSoup
# Custom libs
from exchange_parsers.jpx_public_comments import notice_content_check


class TestJpxPublicComments(unittest.TestCase):
    def test_notice_content_check(self):
        html_string = '<td width="10%"><a href="/english/rules-participants/public-comment/tvdivq000000tmrj-att/policyen.pdf" rel="external"><img ' \
                        'alt="icon-pdf" height="16" src="/english/common/images/icon/tvdivq000000019l-img/icon-pdf.png" title="icon-pdf" ' \
                        'width="16"/></a></td>'
        html = BeautifulSoup(html_string, 'lxml')
        function_result = notice_content_check(html.find_all('td'))
        self.assertEqual(False, function_result)

    def test_notice_content_check_true(self):
        html_string = '<td class="a-center w-space"><!-- /Announcement Date -->                            Dec. 22, 2022                       ' \
                        '   </td>,<td class="a-center w-space"><!-- /Closing Date -->                            Jan. 21, 2023                    ' \
                        '      </td>,<td class="a-center w-space"><!-- /Issued by* -->                            TSE                        ' \
                        '  </td>,<td class="a-left"><a href="/english/rules-participants/public-comment/detail/d02/202121222-01.html">Optimization ' \
                        'of Tick Sizes for Medium Liquidity Stocks</a></td>'

        html = BeautifulSoup(html_string, 'lxml')
        function_result = notice_content_check(html.find_all('td'))
        self.assertEqual(True, function_result)


if __name__ == '__main__':
    unittest.main()
