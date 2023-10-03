# Third party libs
import unittest
# Custom libs
from modules import run_args as ar
ar.test_mode = True
# Framework tests
from .framework.test_data_sources import TestDataSources
from .framework.test_main import TestMain
from .framework.test_regex import TestRegex
from .framework.test_config import TestConfig
from .framework.test_exchange_url_obj import TestExchangeUrlObj
from .framework.test_generic_functions import TestGenericFunctions
from .framework.test_notice_obj import TestNoticeObject
from .framework.test_url_obj import TestUrlObj
from .framework.test_notice_processor import TestNoticeProcessor
from .framework.test_ops_users_cache import TestOpsUsersCache
# Exchange parsers
from .exchange_parsers.test_cboe_euro_derivatives import TestCboeEuroDer
from .exchange_parsers.test_asx import TestAsx
from .exchange_parsers.test_tfex import Testtfex
from .exchange_parsers.test_krx_kind import TestKrxKind
from .exchange_parsers.test_gpw_news import TestGpwNews
from .exchange_parsers.test_kap_pdp import TestKapPdp
from .exchange_parsers.test_athexgroup import TestAthexGroup
from .exchange_parsers.test_gpw_communiques_resolutions import TestGpwCommuniques
from .exchange_parsers.test_jpx_public_comments import TestJpxPublicComments
from .exchange_parsers.test_matbarofex_documents import TestMatbaforexDocuments
from .exchange_parsers.test_mcxinda_trading_surveillance import TestMcxIndia
from .exchange_parsers.test_jse_market_notices import TestJseMarketNotices
from .exchange_parsers.test_meff_fin_cmdty import TestMeffFinCmdty
from .exchange_parsers.test_mgex_announcements import TestMgexAnnouncements
from .exchange_parsers.test_mgex_news_rel import TestMgexNewsRel
from .exchange_parsers.test_nasdaq_dubai_generic import TestNasdaqDubGeneric
from .exchange_parsers.test_nasdaq_generic import TestNasdaqGeneric
from .exchange_parsers.test_jpx_market_news import TestJpxMarketNews


framework_tests = (TestDataSources, TestGenericFunctions, TestMain, TestRegex, TestConfig, TestExchangeUrlObj, TestNoticeProcessor, TestUrlObj,
                   TestOpsUsersCache, TestNoticeObject)
parser_tests = (TestCboeEuroDer, TestAsx, Testtfex, TestKrxKind, TestGpwNews, TestKapPdp, TestAthexGroup, TestGpwCommuniques, TestJpxPublicComments,
                TestMatbaforexDocuments, TestMcxIndia, TestJseMarketNotices, TestMeffFinCmdty, TestMgexAnnouncements, TestMgexNewsRel,
                TestNasdaqDubGeneric, TestNasdaqGeneric, TestJpxMarketNews)


def set_suite():
    suite = unittest.TestSuite()
    for test_class in (*framework_tests, *parser_tests):
        tests = unittest.TestLoader().loadTestsFromTestCase(test_class)
        suite.addTests(tests)
    return suite


if __name__ == '__main__':
    runner = unittest.TextTestRunner()
    runner.run(set_suite())
