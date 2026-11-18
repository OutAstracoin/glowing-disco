import React, { Component } from 'react';
import { Button, ListGroup, ListGroupItem } from 'reactstrap';
import PoserManager from '../PoserManager';

import { ChromePicker, CirclePicker } from 'react-color';

import { Trans } from '@lingui/macro';


export default class MaterialMenu extends Component {
  constructor(props) {
    super(props);
    this.onChangeColor = this.onChangeColor.bind(this);
    this.toggleBodyHair = this.toggleBodyHair.bind(this);
    // this.onSelectNewObject = this.onSelectNewObject.bind(this);
    // this.onSelectNewObject();
    console.log(this.props.curSelectedPart);
    if (this.props.curSelectedPart === 7) {
      this.state = {color: PoserManager.getHairMaterialColor()};
    } else {
      this.state = {color: PoserManager.getMaterialColor(), body: true};
    }
    
  }

  // onSelectNewObject() {
  //   this.state = {color: PoserManager.getMaterialColor()};
  // }

  onChangeColor(color, event) {
    this.setState({color: color});
    console.log(this.props.curSelectedPart);
    if (this.props.curSelectedPart === 7 || this.state.body === false) {
      // head joint
      PoserManager.setHairMaterialColor(color.rgb.r / 255, color.rgb.g / 255, color.rgb.b / 255);
    } else {
      PoserManager.setMaterialColor(color.rgb.r / 255, color.rgb.g / 255, color.rgb.b / 255);
    }
  }

  toggleBodyHair() {
    this.setState({body: !this.state.body})
  }

  render() {
    // console.log("color update");
    // console.log(this.state.color);
    return (
      <div className="side-menu">
        <Button block><Trans>Material</Trans></Button>
        {this.props.curSelectedPart === 5 && <Button block onClick={this.toggleBodyHair}>
          {this.state.body ? <Trans>Body</Trans> : <Trans>Hair</Trans>}
        </Button>}
        <ListGroup>
          <ListGroupItem>
            <ChromePicker className="my-chrome-picker" disableAlpha color={this.state.color} onChange={this.onChangeColor}/>
            <CirclePicker width="100%" color={this.state.color} onChange={this.onChangeColor}/>
            <CirclePicker width="100%" color={this.state.color} onChange={this.onChangeColor}
              colors={
                [
                  "#ffe9dc", 
                  "#fce9db", 
                  "#e0a899", 
                  "#dfa290", 
                  "#c99789",

                  "#fbead4", 
                  "#f9d6c3", 
                  "#dbaf92", 
                  "#a97754", 
                  "#765238",

                  "#ffdbac", 
                  "#f1c27d", 
                  "#e0ac69", 
                  "#c68642", 
                  "#8d5524",
                ]
              }
              marginTop="20px"
            />
          </ListGroupItem>
        </ListGroup>
      </div>
    );
  }
}