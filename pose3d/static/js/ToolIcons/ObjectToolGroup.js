import React, { Component } from 'react';
import { ButtonGroup } from 'reactstrap';

import ToolButton from './ToolButton';
import { faPalette, faTshirt, faLock, faArrowsAlt, faInfinity, faMale, faHandPaper, faWater, faClone, faExchangeAlt, faTrash, faSun, faSyncAlt } from '@fortawesome/free-solid-svg-icons';
import PoserManager from '../PoserManager';

import { Trans } from '@lingui/macro';

// tool button group
export default class ObjectToolGroup extends Component {
  constructor(prop) {
    super(prop);
    // PoserManager.onSelectionChanged = this.onSelectionChanged.bind(this);
    this.state = {curSelectedPart: PoserManager.curSelectedPart};

    this.jointTools = (
      <ButtonGroup>
        <ToolButton icon={faSyncAlt} text={<Trans>rotate</Trans>} onClick={PoserManager.toggleManipulatorRotation}></ToolButton>
      </ButtonGroup>
    );

    this.propObjectTools = (
      <ButtonGroup>
        <ToolButton icon={faPalette} text={<Trans>color</Trans>} onClick={this.props.toggleMaterialMenu}></ToolButton>
        <ToolButton icon={faLock} text={<Trans>lock</Trans>} onClick={PoserManager.toggleLock}></ToolButton>
        <ToolButton icon={faArrowsAlt} text={<Trans>transform</Trans>} onClick={this.props.toggleTransformMenu}></ToolButton>
        <ToolButton icon={faSyncAlt} text={<Trans>rotate</Trans>} onClick={PoserManager.toggleManipulatorRotation}></ToolButton>
        <ToolButton icon={faClone} text={<Trans>duplicate</Trans>} onClick={PoserManager.duplicate}></ToolButton>
        <ToolButton icon={faTrash} text={<Trans>remove</Trans>} onClick={PoserManager.remove}></ToolButton>
      </ButtonGroup>
    );

    this.lightEntityTools = (
      <ButtonGroup>
        <ToolButton icon={faSun} text={<Trans>brightness</Trans>} onClick={this.props.toggleBrightnessMenu}></ToolButton>
        <ToolButton icon={faSyncAlt} text={<Trans>rotate</Trans>} onClick={PoserManager.toggleManipulatorRotation}></ToolButton>
      </ButtonGroup>
    );

    this.hairJointTools = (
      <ButtonGroup>
        <ToolButton icon={faPalette} text={<Trans>color</Trans>} onClick={this.props.toggleMaterialMenu}></ToolButton>
        <ToolButton icon={faWater} text={<Trans>hair</Trans>} onClick={this.props.toggleHairMenu}></ToolButton>
        <ToolButton icon={faSyncAlt} text={<Trans>rotate</Trans>} onClick={PoserManager.toggleManipulatorRotation}></ToolButton>
      </ButtonGroup>
    );

    this.modelTools = (
      <div>
        <ButtonGroup>
          <ToolButton icon={faPalette} text={<Trans>color</Trans>} onClick={this.props.toggleMaterialMenu}></ToolButton>
          {/* <ToolButton icon={faTshirt} text="clothing"></ToolButton> */}
          <ToolButton icon={faWater} text={<Trans>hair</Trans>} onClick={this.props.toggleHairMenu}></ToolButton>
          <ToolButton icon={faLock} text={<Trans>lock</Trans>} onClick={PoserManager.toggleLock}></ToolButton>
          <ToolButton icon={faArrowsAlt} text={<Trans>transform</Trans>} onClick={this.props.toggleTransformMenu}></ToolButton>
          <ToolButton icon={faSyncAlt} text={<Trans>rotate</Trans>} onClick={PoserManager.toggleManipulatorRotation}></ToolButton>
          <ToolButton icon={faMale} text={<Trans>preset</Trans>} onClick={this.props.togglePosePresetList}></ToolButton>
          <ToolButton icon={faHandPaper} text={<Trans>hand</Trans>} onClick={this.props.toggleHandPoseList}></ToolButton>
          <ToolButton icon={faClone} text={<Trans>duplicate</Trans>} onClick={PoserManager.duplicate}></ToolButton>
          <ToolButton icon={faExchangeAlt} text={<Trans>replace</Trans>} onClick={this.props.toggleReplaceModelList}></ToolButton>
          <ToolButton icon={faTrash} text={<Trans>remove</Trans>} onClick={PoserManager.remove}></ToolButton>
        </ButtonGroup>
      </div>
    );

    this.selectedPartId2Tools = [
      (<ButtonGroup></ButtonGroup>)     // Nothing
      , this.propObjectTools   // Prop
      , this.jointTools   // Control
      , this.lightEntityTools   // Light Entity
      , this.jointTools   // Finger
      , this.modelTools   // Model
      , this.jointTools   // Hand
      , this.hairJointTools   // Head
      , this.hairJointTools   // Hair
    ];
  }

  // onSelectionChanged() {
  //   this.setState({
  //     curSelectedPart: PoserManager.curSelectedPart
  //   })
  // }

  render() {
    return (
      <div>
        {/* {this.selectedPartId2Tools[this.state.curSelectedPart]} */}
        {this.selectedPartId2Tools[this.props.curSelectedPart]}
      </div>
    );
  }
}
