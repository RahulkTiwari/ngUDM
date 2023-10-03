# Third party libs
import unittest
# Custom libs
import tests.test_globals  # noqa: Unused import. However, making sure the test_mode is set to True
# Framework tests
from tests.framework.objects.test_security_obj import TestSecuritiesObject, TestSecurityObject
from tests.framework.objects.test_data_source import TestDataSource
from tests.framework.objects.test_source_code import TestSourceCodeObject
from tests.framework.objects.test_write_file import TestWriteFile
from tests.framework.modules.test_generic_functions import TestGenericFunctions
from tests.framework.modules.test_jira import TestJira
from tests.framework.modules.test_config import TestConfig
from tests.framework.modules.test_logger import TestLogger
from tests.framework.modules.test_mongo_connection import TestMongoConnection
from tests.framework.modules.test_ops_users import TestOpsUsersCache
from tests.framework.modules.test_run_args_parser import TestArgsParser
from tests.framework.modules.test_sd_data import TestSdData
from tests.framework.modules.test_source_config import TestSourceConfig
# Parser tests
from tests.parsers.test_byma import TestArgentinaParser
from tests.parsers.test_bolsa_de_santiago import TestChileParser
from tests.parsers.test_cninfo import TestMainLandChinaParser
from tests.parsers.test_hkex_stockconnect import TestChinaOffshoreParser
from tests.parsers.test_nzx import TestNewZealandParser
from tests.parsers.test_bvl import TestPeruParser
from tests.parsers.test_saudi_exchange import TestSaudiArabiaParser


framework_tests = (TestSecuritiesObject, TestSecurityObject, TestGenericFunctions, TestConfig, TestJira, TestLogger, TestSdData, TestOpsUsersCache,
                   TestArgsParser, TestMongoConnection, TestSdData, TestSourceConfig, TestDataSource, TestSourceCodeObject, TestWriteFile)

parser_tests = (TestArgentinaParser, TestChileParser, TestMainLandChinaParser, TestChinaOffshoreParser, TestNewZealandParser, TestPeruParser,
                TestSaudiArabiaParser)


def set_suite():
    suite = unittest.TestSuite()
    for test_class in (*framework_tests, *parser_tests):
        tests = unittest.TestLoader().loadTestsFromTestCase(test_class)
        suite.addTests(tests)
    return suite


if __name__ == '__main__':
    runner = unittest.TextTestRunner()
    runner.run(set_suite())
