# Third party
import unittest
# Custom libs
from objects.url import Url


class TestUrlObj(unittest.TestCase):

    def test_url_wo_parameters(self):
        url_address = 'https://www.taifex.com.tw'
        url_obj = Url(url_address)
        self.assertEqual('https', url_obj.protocol)
        self.assertEqual('www.taifex.com.tw', url_obj.root)
        self.assertIsNone(url_obj.path_extension)
        self.assertEqual({}, url_obj.parameters)

    def test_url_long_wo_parameters(self):
        url_address = 'https://www.taifex.com.tw/file/taifex/eng/eng11/TAIFEXtoLaunch'
        url_obj = Url(url_address)
        self.assertEqual('https', url_obj.protocol)
        self.assertEqual('www.taifex.com.tw/file/taifex/eng/eng11/TAIFEXtoLaunch', url_obj.root)
        self.assertIsNone(url_obj.path_extension)
        self.assertEqual({}, url_obj.parameters)
        self.assertEqual('TAIFEXtoLaunch', url_obj.last_element)
        self.assertEqual(['www.taifex.com.tw', 'file', 'taifex', 'eng', 'eng11', 'TAIFEXtoLaunch'], url_obj.all_elements)

    def test_url_long_w_parameters(self):
        url_address = 'https://www.taifex.com.tw/file/taifex/eng/eng11/TAIFEXtoLaunch?id=123&sep=something'
        url_obj = Url(url_address)
        self.assertEqual('https', url_obj.protocol)
        self.assertEqual('www.taifex.com.tw/file/taifex/eng/eng11/TAIFEXtoLaunch', url_obj.root)
        self.assertIsNone(url_obj.path_extension)
        self.assertEqual({'id': '123', 'sep': 'something'}, url_obj.parameters)

    def test_url_long_w_parameters_and_docx(self):
        url_address = 'https://www.taifex.com.tw/file/taifex/eng/eng11/foobar.docx?id=123&sep=something'
        url_obj = Url(url_address)
        self.assertEqual('https', url_obj.protocol)
        self.assertEqual('www.taifex.com.tw/file/taifex/eng/eng11', url_obj.root)
        self.assertEqual('docx', url_obj.path_extension)
        self.assertEqual('foobar', url_obj.document_root)
        self.assertEqual('foobar.docx', url_obj.document_full)
        self.assertEqual({'id': '123', 'sep': 'something'}, url_obj.parameters)
        self.assertEqual('eng11', url_obj.last_element)

    def test_url_long_w_parameters_and_html(self):
        url_address = 'https://www.taifex.com.tw/file/taifex/eng/eng11/foobar.html'
        url_obj = Url(url_address)
        self.assertEqual('https', url_obj.protocol)
        self.assertEqual('www.taifex.com.tw/file/taifex/eng/eng11', url_obj.root)
        self.assertEqual('html', url_obj.path_extension)
        self.assertEqual('foobar', url_obj.document_root)
        self.assertEqual('foobar.html', url_obj.document_full)
        self.assertEqual('eng11', url_obj.last_element)
        self.assertEqual({}, url_obj.parameters)

    def test_url_spec_chars(self):
        url_address = 'http://www.taifex.com.tw/file/taifex/eng/eng11/foo&bar'
        url_obj = Url(url_address)
        self.assertEqual('http', url_obj.protocol)
        self.assertEqual('www.taifex.com.tw/file/taifex/eng/eng11/foo&bar', url_obj.root)
        self.assertIsNone(url_obj.path_extension)
        self.assertIsNone(url_obj.document_root)
        self.assertIsNone(url_obj.document_full)
        self.assertEqual({}, url_obj.parameters)


if __name__ == '__main__':
    unittest.main()
