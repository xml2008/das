/**
 * Created by liang.wang on 19/12/12.
 */
import React from 'react'
import {Col, Row, Button} from 'antd'
import {Itemlabel, InputAntPlus} from '../../../../utils/Index'
import QueryBaseManageCloudPanle from '../../base/QueryBaseManageCloudPanle'

export default class AppListByDbNameHeader extends QueryBaseManageCloudPanle {

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
            disabled: true
        }
    }

    render() {
        const {disabled} = this.state
        const _props = this.initProps()
        const {children} = this.props
        const data = this.getValueToJson(this.searchInfoData)
        return <div style={{padding: '10px 0px 0px 0px'}}>
            <Row style={{padding: '0px 0px 10px 0px'}}>
                <Col sm={20} style={{fontSize: '17px', fontWeight: 'bolder'}}>
                    <Itemlabel title='物理库名 : ' type={2} formLayout={this.topicNames} style={{marginBottom: '0px'}}>
                        <InputAntPlus {..._props} valueLink={this.searchInfoData} placeholder='请输入物理库名，多个用逗号隔开，中英文逗号皆可'
                                      value={data} onChangeCallBack={v => this.onChangeCallBack(v, this)}/>
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
