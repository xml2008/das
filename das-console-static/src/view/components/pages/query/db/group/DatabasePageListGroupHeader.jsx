/**
 * Created by liang.wang on 19/12/12.
 */
import React from 'react'
import {Col, Row, Button} from 'antd'
import {urls, dataConfig} from '../../../../../../model/base/BaseModel'
import {Itemlabel, SelectSinglAjax} from '../../../../utils/Index'
import QueryBaseManageCloudPanle from '../../base/QueryBaseManageCloudPanle'

export default class DatabasePageListGroupHeader extends QueryBaseManageCloudPanle {

    constructor(props, context) {
        super(props, context)
        this.modelName = props.modelName
        this.initValueLink()
        this.topicNames = {
            labelCol: {
                sm: {span: 4}
            },
            wrapperCol: {
                sm: {span: 20}
            }
        }
        this.state = {
            groups: [],
            disabled: true
        }

        this.initGroupData()
    }

    initGroupData = () => {
        this.props.getItem(urls.type.group_tree, {}, this, data => {
            if (data.code === dataConfig.sucCode) {
                this.setState({groups: data.msg})
            }
        })

    }

    onChangeCallbBack = v => {
        if (v) {
            this.setState({disabled: false})
        }
    }

    createSelectSingleGroup = () => {
        const {groups} = this.state
        const items = []
        groups.forEach(i => {
            items.push({id: i.id, name: i.group_name + ' : ' + i.group_comment})
        })
        const _props = this.initProps()
        const item = this.getValueToJson(this.searchInfoData)
        return <SelectSinglAjax {..._props} valueLink={this.searchInfoData}
                                isSearch={true} items={items} selectedId={item}
                                onChangeCallbBack={this.onChangeCallbBack}/>
    }

    render() {
        const {disabled} = this.state
        const {children} = this.props
        return <div style={{padding: '10px 0px 0px 0px'}}>
            <Row style={{padding: '0px 0px 10px 0px'}}>
                <Col sm={20} style={{fontSize: '17px', fontWeight: 'bolder'}}>
                    <Itemlabel title='项目组 : ' type={2} formLayout={this.topicNames} style={{marginBottom: '0px'}}>
                        {this.createSelectSingleGroup()}
                    </Itemlabel>
                </Col>
                <Col sm={4} style={{display: 'grid', paddingLeft: '10px'}}>
                    <Button icon='search' type='primary' size='large' onClick={() => this.search(this)}
                            disabled={disabled}>查询</Button>
                </Col>
            </Row>
            <Row>
                <Col sm={24} style={{display: 'grid'}}>
                    {children}
                </Col>
            </Row>
        </div>
    }
}
