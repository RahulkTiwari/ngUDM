# Third party libs
import unittest
# Custom libs
from exchange_parsers.cboe_euro_derivates import set_values


class TestCboeEuroDer(unittest.TestCase):
    def test_set_values(self):
        notice_json = {
            "id": 44556,
            "section": "relse_note",
            "add_ts": "2023-02-10 15:56:21.185489-05:00",
            "icon": "pdf",
            "link": "https://cdn.cboe.com/resources/release_notes/2023/Cboe-Europe-Derivatives-CEDX-Environment-Weekend-Maintenance-February.pdf",
            "publication_dt": "2023-02-10",
            "source": "",
            "file_key": "resources/release_notes/2023/Cboe-Europe-Derivatives-CEDX-Environment-Weekend-Maintenance-February.pdf",
            "show_on_global_site": False,
            "article_id": 44556,
            "mkt": "cedx",
            "show_on_home_page": False,
            "message_rss_ts": "Fri, 10 Feb 2023 00:00:00",
            "subject": "Cboe Europe Derivatives CEDX Weekend Maintenance February/March 2023",
            "message": "Please be advised that Cboe Europe (Cboe) will be performing weeken",
            "message_ts": "February 10, 2023",
            "isExternalWebsite": True
        }
        notice = set_values(notice_json)

        self.assertEqual(notice.noticeSourceUniqueId, '44556')

    def test_set_values_keyerror(self):
        notice_json = {
            "id": 44556,
            "section": "relse_note",
            "add_ts": "2023-02-10 15:56:21.185489-05:00",
            "icon": "pdf",
            "link": "https://cdn.cboe.com/resources/release_notes/2023/Cboe-Europe-Derivatives-CEDX-Environment-Weekend-Maintenance-February.pdf",
            "source": "",
            "file_key": "resources/release_notes/2023/Cboe-Europe-Derivatives-CEDX-Environment-Weekend-Maintenance-February.pdf",
            "show_on_global_site": False,
            "article_id": 44556,
            "mkt": "cedx",
            "show_on_home_page": False,
            "message_rss_ts": "Fri, 10 Feb 2023 00:00:00",
            "subject": "Cboe Europe Derivatives CEDX Weekend Maintenance February/March 2023",
            "message": "Please be advised that Cboe Europe (Cboe) will be performing weeken",
            "message_ts": "February 10, 2023",
            "isExternalWebsite": True
        }
        notice = set_values(notice_json)

        self.assertEqual('44556', notice.noticeSourceUniqueId)
        self.assertEqual('Cboe Europe Derivatives CEDX Weekend Maintenance February/March 2023', notice.eventSubject)
        self.assertIsNone(notice.stormKey)
        self.assertFalse(notice.valid_notice)


if __name__ == '__main__':
    unittest.main()
