import React, { Component } from 'react';
import { Button } from 'reactstrap';

// import p0 from './imgs/body_poses/image_00.png';
import PoserManager from '../PoserManager';
import ModelList from './ModelList';

import { Trans } from '@lingui/macro';

export default class ReplaceModelMenu extends Component {
  // constructor(props) {
  //   super(props);
  //   this.toggle = this.toggle.bind(this);
  //   this.state = { unfold: -1 };
  // }

  // toggle() {
  //   this.props.toggleParent();
  // }

  render() {
    return (
      <div className="side-menu">
        <Button block><Trans>Replace Model</Trans></Button>
        <ModelList onClickFunc={PoserManager.changeModel} />
      </div>
    );
  }
}