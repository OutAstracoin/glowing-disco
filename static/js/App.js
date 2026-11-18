import React, { Component } from 'react';
// import logo from './logo.svg';
import './App.css';
import 'rc-slider/assets/index.css';

import { ButtonGroup, ButtonToolbar, UncontrolledDropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap';
import { faUndo, faRedo, faPlus, faCamera } from '@fortawesome/free-solid-svg-icons';

import ToggleSettings from './ToolIcons/ToggleSettings';
import ViewSettings from './ToolIcons/ViewSettings';
import ObjectToolGroup from './ToolIcons/ObjectToolGroup';
import ToolButton from './ToolIcons/ToolButton';
import PoserManager, { SelectedPart } from './PoserManager';

import AddObjectListMenu from './SideMenuList/AddObjectListMenu';
import PosePresetListMenu from './SideMenuList/PosePresetListMenu';
import HandPoseListMenu from './SideMenuList/HandPoseListMenu';
import ReplaceModelMenu from './SideMenuList/ReplaceModelMenu';
import { TransformModelMenu, TransformPropMenu } from './SideMenuList/TransformMenu';
import PerspectiveMenu from './SideMenuList/PerspectiveMenu';
import BrightnessMenu from './SideMenuList/BrightnessMenu';
import MaterialMenu from './SideMenuList/MaterialMenu';
import HairMenu from './SideMenuList/HairMenu';

import { Trans } from '@lingui/macro';

// import './renderer/emscripten-progress';
// import './renderer/index';

const SideMenu = {
  None: -1,
  AddMenu: 0,
  PosePresetMenu: 1,
  HandMenu: 2,
  SwapModelMenu: 3,
  TransformMenu: 4,
  PerspectiveMenu: 5,
  BrightnessMenu: 6,
  MaterialMenu: 7,
  HairMenu: 8
};

class App extends Component {

  constructor(props) {
    super(props);
    this.toggleAddObjectList = this.toggleAddObjectList.bind(this);
    this.togglePosePresetList = this.togglePosePresetList.bind(this);
    this.toggleReplaceModelList = this.toggleReplaceModelList.bind(this);
    this.toggleHandPoseList = this.toggleHandPoseList.bind(this);
    this.toggleTransformMenu = this.toggleTransformMenu.bind(this);
    this.togglePerspectiveMenu = this.togglePerspectiveMenu.bind(this);
    this.toggleBrightnessMenu = this.toggleBrightnessMenu.bind(this);
    this.toggleMaterialMenu = this.toggleMaterialMenu.bind(this);
    this.toggleHairMenu = this.toggleHairMenu.bind(this);
    PoserManager.onSelectionChanged = this.onSelectionChanged.bind(this);
    PoserManager.onCanvasClick = this.onCanvasClick.bind(this);
    PoserManager.onPreviewModeOn = this.onPreviewModeOn.bind(this);
    // this.state = { showAddObjectList: false };
    this.state = {
      hide: false,
      sideMenu: -1,
      curSelectedPart: 0
    };

    // this.forceKey = 0;
  }

  onCanvasClick() {
    if (this.state.hide === true) {
      this.setState({ hide: false });
      PoserManager.disablePreviewMode();
    }

    this.setState({ sideMenu: -1 });
    // this.forceKey++;
  }

  onPreviewModeOn() {
    this.setState({ hide: true });
  }

  onSelectionChanged(s) {
    // console.log("app#onSelectionChanged")
    this.setState({
      // sideMenu: this.state.sideMenu,
      curSelectedPart: s
    });
  }

  toggleMenu(menuToShow) {
    this.setState({ sideMenu: this.state.sideMenu === menuToShow ? SideMenu.None : menuToShow });
  }

  toggleAddObjectList() {
    // let cur = this.state.sideMenu;
    // this.setState({ sideMenu: cur === SideMenu.AddMenu ? SideMenu.None : SideMenu.AddMenu });
    this.toggleMenu(SideMenu.AddMenu);
  }

  togglePosePresetList() {
    this.toggleMenu(SideMenu.PosePresetMenu);
  }

  toggleHandPoseList() {
    let cur = this.state.sideMenu;
    this.toggleMenu(SideMenu.HandMenu);
    PoserManager.setHandMode(cur === SideMenu.HandMenu ? false : true);
  }

  toggleReplaceModelList() {
    this.toggleMenu(SideMenu.SwapModelMenu);
  }

  toggleTransformMenu() {
    this.toggleMenu(SideMenu.TransformMenu);
  }

  togglePerspectiveMenu() {
    this.toggleMenu(SideMenu.PerspectiveMenu);
  }

  toggleBrightnessMenu() {
    this.toggleMenu(SideMenu.BrightnessMenu);
  }

  toggleMaterialMenu() {
    this.toggleMenu(SideMenu.MaterialMenu);
  }

  toggleHairMenu() {
    this.toggleMenu(SideMenu.HairMenu);
  }

  render() {

    if (this.state.hide === true) {
      return (<div className="App"></div>);
    } else {
      return (
        <div className="App">
          {/* <header className="App-header">
            <img src={logo} className="App-logo" alt="logo" />
            <p>
              Edit <code>src/App.js</code> and save to reload.
            </p>
            <a
              className="App-link"
              href="https://reactjs.org"
              target="_blank"
              rel="noopener noreferrer"
            >
              Learn React
            </a>
          </header> */}


          {/* <Toolbar /> */}

          <div onClick={() => window.open("https://magicposer.com", "_blank")} className="back-to-site"><span><Trans id="try-link"></Trans></span></div>
          <ButtonToolbar>
            <ButtonGroup>
              {/* <ToolButton icon={faPlus} text="add" onClick={this.toggleAddObjectList}></ToolButton> */}
              <ToolButton icon={faPlus} text={<Trans>add</Trans>} onClick={this.toggleAddObjectList}></ToolButton>
            </ButtonGroup>
            <ButtonGroup>
              {/* <ToolButton icon={faHome} text="home"></ToolButton> */}
              <ToolButton icon={faCamera} text={<Trans>preview</Trans>} onClick={PoserManager.enablePreviewMode}></ToolButton>
            </ButtonGroup>
            <ButtonGroup>
              <ToolButton icon={faUndo} text={<Trans>undo</Trans>} onClick={PoserManager.undo}></ToolButton>
              <ToolButton icon={faRedo} text={<Trans>redo</Trans>} onClick={PoserManager.redo}></ToolButton>
            </ButtonGroup>
            <ToggleSettings updateLanguage={this.props.updateLanguage}></ToggleSettings>
            <ViewSettings togglePerspectiveMenu={this.togglePerspectiveMenu}></ViewSettings>
            {/* <ObjectToolGroup togglePosePresetList={this.togglePosePresetList.bind(this)}></ObjectToolGroup> */}
            <ObjectToolGroup
              togglePosePresetList={this.togglePosePresetList}
              toggleHandPoseList={this.toggleHandPoseList}
              toggleReplaceModelList={this.toggleReplaceModelList}
              toggleTransformMenu={this.toggleTransformMenu}
              toggleBrightnessMenu={this.toggleBrightnessMenu}
              toggleMaterialMenu={this.toggleMaterialMenu}
              toggleHairMenu={this.toggleHairMenu}
              curSelectedPart={this.state.curSelectedPart}
            ></ObjectToolGroup>

            {/* <ButtonGroup float="right"> */}
            
            {/* <ButtonGroup>
              <UncontrolledDropdown>
                <DropdownToggle caret>
                  <Trans>Language</Trans>
                </DropdownToggle>
                <DropdownMenu right>
                  <DropdownItem onClick={() => this.props.updateLanguage('en')}>English</DropdownItem>
                  <DropdownItem onClick={() => this.props.updateLanguage('zh')}>中文</DropdownItem>
                  <DropdownItem onClick={() => this.props.updateLanguage('ja')}>日本語</DropdownItem>
                </DropdownMenu>
              </UncontrolledDropdown>
            </ButtonGroup> */}
          </ButtonToolbar>

          {this.state.sideMenu === SideMenu.AddMenu && <AddObjectListMenu />}
          {this.state.sideMenu === SideMenu.PerspectiveMenu && <PerspectiveMenu togglePerspectiveMenu={this.togglePerspectiveMenu} />}


          {this.state.sideMenu === SideMenu.PosePresetMenu && this.state.curSelectedPart === SelectedPart.SkelModel && <PosePresetListMenu />}
          {this.state.sideMenu === SideMenu.HandMenu && this.state.curSelectedPart === SelectedPart.SkelModel && <HandPoseListMenu />}
          {this.state.sideMenu === SideMenu.SwapModelMenu && this.state.curSelectedPart === SelectedPart.SkelModel && <ReplaceModelMenu />}

          {/* TODO: needs to force refresh Transform when selection changed*/}
          {this.state.sideMenu === SideMenu.TransformMenu && this.state.curSelectedPart === SelectedPart.SkelModel && <TransformModelMenu />}
          {this.state.sideMenu === SideMenu.TransformMenu && this.state.curSelectedPart === SelectedPart.Model && <TransformPropMenu />}

          {this.state.sideMenu === SideMenu.BrightnessMenu && this.state.curSelectedPart === SelectedPart.Light && <BrightnessMenu toggleBrightnessMenu={this.toggleBrightnessMenu} />}
          {this.state.sideMenu === SideMenu.MaterialMenu && (this.state.curSelectedPart === SelectedPart.Model || this.state.curSelectedPart === SelectedPart.SkelModel || this.state.curSelectedPart === SelectedPart.Head) && <MaterialMenu curSelectedPart={this.state.curSelectedPart} />}
          {this.state.sideMenu === SideMenu.HairMenu && (this.state.curSelectedPart === SelectedPart.Head || this.state.curSelectedPart === SelectedPart.SkelModel) && <HairMenu />}

        </div>
      );
    }

  }
}

export default App;
