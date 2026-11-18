import React, { Component } from 'react';
import { Button } from 'reactstrap';
import ItemCard from './ItemCard';

// import p0 from './imgs/body_poses/image_00.png';
import PoserManager from '../PoserManager';

import { Trans } from '@lingui/macro';


const poseImgs = importAll(require.context('../imgs/body_poses', false, /\.(png|jpe?g|svg)$/));

function importAll(r) {
  return r.keys().map(r);
}




export default class PosePresetListMenu extends Component {
  // constructor(props) {
  //   super(props);
  //   this.toggle = this.toggle.bind(this);
  //   this.state = { unfold: -1 };
  // }

  // toggle() {
  //   this.props.toggleParent();
  // }

  render() {
    let poses = [];
    for (let i = 0, len = poseImgs.length; i < len; i++) {
      poses.push(
        <ItemCard 
          key={i}
          poserFunc={PoserManager.applyBodyPose}
          src={i}
          img={poseImgs[i]}>
        </ItemCard>);
    }

    return (
      <div className="side-menu">
        <Button block><Trans>Pose Presets</Trans></Button>
        <div className="list">
          {/* <ItemCard
            poserFunc={PoserManager.applyBodyPose}
            src={0}
            img={p0}>
          </ItemCard> */}
          {poses}
        </div>
      </div>
    );
  }
}