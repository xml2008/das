import {Model} from 'ea-react-dm-v14'

@Model('DatabaseBatchModel')
export default class DatabaseBatchModel {

    static rs

    static states = {
        item: {},
        title: '',
        isAlert: false,
        loading: false,
        editeVisible: false
    }

    static searchResultList = {
        list: [],
        searchInfo: {
            totalCount: 0,
            pageSize: 10,
            page: 1,
            data: {
                db_address: '',
                db_port: '',
                db_catalog: ''
            }
        }
    }


    static columnInfo = {
        column: [
            {
                name: '数据库名',
                width: 20,
                key: 'db_catalog'
            },
            {
                name: '地址',
                width: 20,
                key: 'db_address'
            },
            {
                name: '端口',
                width: 10,
                key: 'db_port'
            },
            {
                name: '数据库标识符',
                width: 15,
                key: 'dbname',
                sort: true,
                sortKey: 'db_name'
            },
            {
                name: '类型',
                width: 5,
                map: {1: 'MySql', 2: 'SQLServer'},
                key: 'db_type',
                sort: true
            },
            {
                name: '所属组',
                width: 10,
                key: 'group_name',
                sort: true,
                sortKey: 'dal_group_id'
            },
            {
                name: '创建时间',
                width: 10,
                key: 'insert_time',
                sort: true,
                timePicker: {type: 'range'}
            },
            {
                name: '操作人',
                width: 10,
                key: 'userRealName',
                sort: true,
                sortKey: 'user_real_name',
                search: false
            }
        ]
    }

}