# Third party libraries
import unittest
# Custom libraries
from libraries.objects.RunArguments import RunArguments
from libraries.retrieve_notifications import retrieve_notices_from_db


class RetrieveNotices(unittest.TestCase):

    def test_retrieving_notices_from_db_init(self):
        run_arguments = RunArguments()
        run_arguments.mode = 'init'
        run_arguments.date = '19700101 00:00:00'

        function_result = retrieve_notices_from_db(run_arguments)
        self.assertEqual(1061, len([x for x in function_result]))

    def test_retrieving_notices_from_db_init_later_date(self):
        run_arguments = RunArguments()
        run_arguments.mode = 'init'
        run_arguments.date = '20181101 00:00:00'
        function_result = retrieve_notices_from_db(run_arguments)
        self.assertEqual(961, len([x for x in function_result]))

    def test_retrieving_notices_from_db_delta(self):
        run_arguments = RunArguments()
        run_arguments.mode = 'delta'
        run_arguments.date = '19700101 00:00:00'
        function_result = retrieve_notices_from_db(run_arguments)
        self.assertEqual(1061, len([x for x in function_result]))

    def test_retrieving_notices_from_db_delta_later_date(self):
        run_arguments = RunArguments()
        run_arguments.mode = 'delta'
        run_arguments.date = '20181101 00:00:00'
        function_result = retrieve_notices_from_db(run_arguments)
        self.assertEqual(961, len([x for x in function_result]))


if __name__ == '__main__':
    unittest.main()
