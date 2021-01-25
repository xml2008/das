import {UserEnv} from '../../view/components/utils/util/Index'

export const dasConfig = {
    superid: 1
}

export const dataConfig = {
    sucCode: 200,
    errCode: 500
}

export const dataListConfig = {
    searchResultList: '.searchResultList.list',
    searchInfo: '.searchResultList.searchInfo',
    columnInfo: '.columnInfo',
    columnInfoBack: '.columnInfoBack'
}

export const sysnc = {
    token: 'token=87679214010892'
}

export const column = {
    visible: true,
    sortArrow: 'both',
    classNametitle: 'text-align-center col-div-width col-color-hover noselect',
    classNameColumn: 'text-align-center col-div-width'
}

export const storageCode = {
    loadUserMenustorageCode: 'loadUserMenustorageCode',
    chartsdata: 'chartsdata',
    dataSource: 'dataSource'
}

export const das_msg = {
    apollo_namespace: () => {
        return '只能由小写英文字母,数字,下划线组成，' + UserEnv.getConfigCenterName() + 'NAMESPACE最长25个字符'
    },
    ordinary_name: '只能由英文字母,数字,下划线组成',
    class_name: '只能由英文字母,数字,点组成',
    project_name: '只能由英文字母,数字,点,下划线组成'
}

export const dataFieldTypeEnum = {
    sql_date: 11,
    util_date: 12
}

export const display = {
    buttons_path: '.displayItems.buttons',
    custom: ['catalogs', 'saec', 'detail', 'download', 'simLogin'],
    buttons: {
        add: 'showAddButton',
        editor: 'showEditorButton',
        delete: 'showDeleteButton',
        download: 'showDownloadButton',
        sync: 'showSyncButton',
        check: 'showCkeckButton',
        saec: 'showSaecButton',
        detail: 'showDetailButton',
        catalogs: 'showCatalogsButton',
        simLogin: 'showSimLoginButton',
        checkAll: 'showCkeckAllButton'
    }
}

export const databaseTypes = [{id: 1, name: 'mySql'}, {id: 2, name: 'sqlServer'}]

export const patternTypes = [{id: 0, name: '否'}, {id: 1, name: '是'}]

export const databaseShardingTypes = [{id: 1, name: 'Master'}, {id: 2, name: 'Slave'}]

export const roleTypes = [{id: 1, name: '管理员'}, {id: 2, name: '普通成员'}]

export const strategyType = [{id: 1, name: '静态加载的策略'}, {id: 2, name: '动态加载策略'}]

/*export const strategyDbsetType = [{id: 0, name: '无策略'}, {id: 1, name: '私有策略'}, {id: 2, name: '公共策略'}]*/

export const strategyDbsetType = [{id: 0, name: '无策略'}, {id: 1, name: '私有策略'}]

export const serverEnabled = [{id: 0, name: '否'}, {id: 1, name: '是'}]

export const fieldTypes = [{id: 11, name: 'java.sql.Timestamp'}, {id: 12, name: 'java.util.Date'}]

export const autoReloadEnabledTypes = [{id: 0, name: '否'}, {id: 1, name: '是'}]

export const envButtons = [
    {id: 0, name: 'DEV', value: 'DEV'},
    {id: 1, name: 'FAT', value: 'FAT'},
    {id: 2, name: 'UAT', value: 'UAT'},
    {id: 3, name: 'PRE', value: 'PRE'},
    {id: 4, name: 'PRO', value: 'PRO'}
]

export const urls = {
    getUrl: type => {
        switch (type) {
            case 'db_page_list':
                return UserEnv.getAppUrl() + '/db/page/list'
            case 'db_update_batch':
                return UserEnv.getAppUrl() + '/db/update/batch'
            case 'db_page_by_appids':
                return UserEnv.getAppUrl() + '/compound/query/db/page/by/appids'
            case 'db_page_by_group_ids':
                return UserEnv.getAppUrl() + '/compound/query/db/page/by/group/ids'
            case 'app_page_by_db_names':
                return UserEnv.getAppUrl() + '/compound/query/app/page/by/db/names'
            case 'dbset_page_by_db_names':
                return UserEnv.getAppUrl() + '/compound/query/dbset/page/by/db/names'
            case 'app_page_by_dbset_names':
                return UserEnv.getAppUrl() + '/compound/query/app/page/by/dbset/names'
            case 'db_page_by_dbset_names':
                return UserEnv.getAppUrl() + '/compound/query/db/page/by/dbset/names'
            case 'group_tree':
                return UserEnv.getAppUrl() + '/compound/query/group/tree'
        }
    },
    type: {
        db_page_list: 'db_page_list',
        db_update_batch: 'db_update_batch',
        db_page_by_appids: 'db_page_by_appids',
        db_page_by_group_ids: 'db_page_by_group_ids',
        app_page_by_db_names: 'app_page_by_db_names',
        dbset_page_by_db_names: 'dbset_page_by_db_names',
        app_page_by_dbset_names: 'app_page_by_dbset_names',
        db_page_by_dbset_names: 'db_page_by_dbset_names',
        group_tree: 'group_tree',
    }
}

