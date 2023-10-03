# Third party
import unittest
# Custom libs
from exchange_parsers.athexgroup import get_ref_id


class TestAthexGroup(unittest.TestCase):
    def test_get_ref_id(self):
        url_string = 'https://www.athexgroup.gr/web/guest/members-trading-deriv-corp-actions/-/asset_publisher/la40Cn0UJnTr/document/id/6813902?' \
                     'controlPanelCategory=portlet_101_INSTANCE_la40Cn0UJnTr&amp;redirect=https%3A%2F%2Fwww.athexgroup.gr%2Fweb%2Fguest%2Fmembers' \
                     '-trading-deriv-corp-actions%3Fp_p_id%3D101_INSTANCE_la40Cn0UJnTr%26p_p_lifecycle%3D0%26p_p_state%3Dnormal%26p_p_mode' \
                     '%3Dview%26controlPanelCategory%3Dportlet_101_INSTANCE_la40Cn0UJnTr%26_101_INSTANCE_la40Cn0UJnTr_'
        function_result = get_ref_id(url_string)
        self.assertEqual('6813902', function_result)


if __name__ == '__main__':
    unittest.main()
