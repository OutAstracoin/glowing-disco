import React, { Component } from 'react';
import { Button } from 'reactstrap';
import ItemCard from './ItemCard';

// import p0 from './imgs/body_poses/image_00.png';
import PoserManager from '../PoserManager';

import { Trans } from '@lingui/macro';


const poseImgs = importAll(require.context('../imgs/hand_poses', false, /\.(png|jpe?g|svg)$/));

function importAll(r) {
  return r.keys().map(r);
}




export default class HandPoseListMenu extends Component {
  constructor(props) {
    super(props);
    this.toggleLeftRight = this.toggleLeftRight.bind(this);
    this.applyHandPose = this.applyHandPose.bind(this);
    this.state = { left: true };
  }

  toggleLeftRight() {
    this.setState({left: !this.state.left});
  }

  applyHandPose(id) {
    PoserManager.applyHandPose(id, this.state.left);
  }

  render() {
    let poses = [];
    for (let i = 0, len = poseImgs.length; i < len; i++) {
      poses.push(
        <ItemCard 
          key={i}
          poserFunc={this.applyHandPose}
          src={i}
          img={poseImgs[i]}>
        </ItemCard>);
    }

    return (
      <div className="side-menu">
        <Button block><Trans>Hand Pose</Trans></Button>
        <Button block onClick={this.toggleLeftRight}>
          {this.state.left ? <Trans>Left</Trans> : <Trans>Right</Trans>}
        </Button>
        <div className="list">
          {poses}
        </div>
      </div>
    );
  }
}