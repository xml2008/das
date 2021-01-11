import {Model} from 'ea-react-dm-v14'

@Model('AppListByDbNameModel')
export default class AppListByDbNameModel {

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
            data: ''
        }
    }


    static columnInfo = {
        column: [
            {
                name: '所属组',
                width: 10,
                key: 'group_name'
            },
            {
                name: 'APPID',
                width: 10,
                key: 'app_id'
            },
            {
                name: '项目名',
                width: 10,
                key: 'name'
            },
            {
                name: '物理库名',
                width: 40,
                key: 'db_catalog'
            },
            {
                name: '负责人',
                width: 15,
                key: 'user_name'
            },
            {
                name: '备注',
                width: 15,
                key: 'comment'
            }
        ]
    }

}