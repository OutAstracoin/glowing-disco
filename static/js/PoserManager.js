
// manage poser states and interface functions to wasm renderer (poser)
// essentially a singleton

// when import, call
// import PoserManager from './PoserManager'

// enum SelectedPart
  // Nothing  = 0
  // Model    = 1  // prop
  // Control  = 2
  // LightEnt = 3
  // Finger   = 4
  // SkelModel= 5  // model
  // Hand     = 6
  // Head     = 7
  // Hair     = 8
export const SelectedPart = {
  Nothing: 0,
  Model: 1,
  Control: 2,
  Light: 3,
  Finger: 4,
  SkelModel: 5,
  Hand: 6,
  Head: 7,
  Hair: 8
};

var PoserManager =  {
  canvasOnClick() {
    // console.log('clicked');
    if (!window.wasmReady) return;
    if (PoserManager.onCanvasClick) {
      PoserManager.onCanvasClick();
    }
    PoserManager.getSelectedPart();
  },

  enablePreviewMode() {
    if (!window.wasmReady) return;

    window.wasm.SetPreviewMode(1);
    if (PoserManager.onPreviewModeOn) {
      PoserManager.onPreviewModeOn();
    }
  },

  disablePreviewMode() {
    if (!window.wasmReady) return;
    window.wasm.SetPreviewMode(0);
  },

  // functions called by javascript app (like toolbar buttons)
  curSelectedPart: 0,
  onSelectionChanged: null,  // stub for refresh ui
  onCanvasClick: null,
  onPreviewModeOn: null,

  getSelectedPart() {
    // // !!!: not safe
    // var newSelectedPart = window.wasm.GetSelectedPart();
    // if (this.curSelectedPart !== newSelectedPart) {
    //   this.curSelectedPart = newSelectedPart;
    //   if (this.onSelectionChanged) {
    //     this.onSelectionChanged(this.curSelectedPart);
    //   }
    // }
    // return this.curSelectedPart;
    if (!window.wasmReady) return;

    PoserManager.curSelectedPart = window.wasm.GetSelectedPart();
    if (PoserManager.onSelectionChanged) {
      PoserManager.onSelectionChanged(PoserManager.curSelectedPart);
    }

    // console.log(this.curSelectedPart);
  },

  // enum ControlOOptions
  // Rotate   = 0
  // Translate= 1
  // Both     = 2
  // None     = 3
  getControlDOF() {
    if (!window.wasmReady) return 3;
    return window.wasm.GetControlDOF();
  },

  duplicate() {
    if (!window.wasmReady) return;
    window.wasm.Duplicate();
  },

  remove() {
    if (!window.wasmReady) return;
    window.wasm.Remove();
  },

  undo() {
    if (!window.wasmReady) return;
    window.wasm.Undo();
  },
  redo() {
    if (!window.wasmReady) return;
    window.wasm.Redo();
  },

  addModel(name) {
    if (!window.wasmReady) return;
    window.wasm.AddModel(name);
  },
  addProp(name) {
    if (!window.wasmReady) return;
    window.wasm.AddProp(name);
  },
  changeModel(name) {
    if (!window.wasmReady) return;
    window.wasm.ChangeModel(name);
  },


  // getRotAxes() {
  //   window.wasm.GetRotAxes(1, 1, 1);
  // },

  // ManipulatorMode
  // Translate  = 0
  // Rotate     = 1
  // Scale      = 2
  toggleManipulatorRotation() {
    if (!window.wasmReady) return;
    var mode = window.wasm.GetManipulatorMode();
    window.wasm.SetManipulatorMode(mode === 1 ? 0 : 1);
  },


  // toggleHandMode() {
  //   var enabled = window.wasm.GetFingerMode() === 0 ? 1 : 0;
  //   window.wasm.ToggleHandMode(enabled);
  //   return enabled;
  // },

  setHandMode(enabled) {
    if (!window.wasmReady) return;
    window.wasm.ToggleHandMode(enabled);
  },


  applyHandPose(i, left) {
    if (!window.wasmReady) return;
    if (left) {
      window.wasm.ApplyLeftHandPose(i);
    } else {
      window.wasm.ApplyRightHandPose(i);
    }
  },
  applyBodyPose(i) {
    if (!window.wasmReady) return;
    window.wasm.ApplyBodyPose(i);
  },

  addHair(hair) {
    if (!window.wasmReady) return;
    window.wasm.AddHair(hair);
  },
  removeHair() {
    if (!window.wasmReady) return;
    window.wasm.RemoveHair();
  },



  setPerspective(fov) {
    if (!window.wasmReady) return;
    window.wasm.SetPerspective(fov);
  },
  getPerspective() {
    if (!window.wasmReady) return 30.0;
    return window.wasm.GetPerspective();
  },
  frameSelected() {
    if (!window.wasmReady) return;
    window.wasm.FrameSelected();
  },
  setView(i) {
    if (!window.wasm) return;
    window.wasm.SetView(i);
  },


  // for character models
  setScale(s) {
    if (!window.wasmReady) return;
    window.wasm.SetScale(s);
  },
  getScale() {
    if (!window.wasmReady) return;
    return window.wasm.GetScale();
  },

  setPropScale(x, y, z) {
    if (!window.wasmReady) return;
    window.wasm.SetPropScale(x, y, z);
  },
  getPropScale() {
    var s = [0.0, 0.0, 0.0];
    if (!window.wasmReady) return s;
    var offset = window.wasm.GetPropScale();
    s[0] = window.wasm.getValue(offset, 'float');
    s[1] = window.wasm.getValue(offset + 4, 'float');
    s[2] = window.wasm.getValue(offset + 8, 'float');
    return s;
  },


  setObjectPos(x, y, z) {
    if (!window.wasmReady) return;
    window.wasm.SetObjectPos(x, y, z);
  },
  getObjectPos() {
    var pos = [0.0, 0.0, 0.0];
    if (!window.wasmReady) return pos;
    var offset = window.wasm.GetObjectPos();
    pos[0] = window.wasm.getValue(offset, 'float');
    pos[1] = window.wasm.getValue(offset + 4, 'float');
    pos[2] = window.wasm.getValue(offset + 8, 'float');
    return pos;
  },

  setBrightness(v) {
    if (!window.wasmReady) return;
    window.wasm.SetBrightness(v);
  },
  getBrightness() {
    if (!window.wasmReady) return 1.0;
    return window.wasm.GetBrightness();
  },
  setGIStrength(v) {
    if (!window.wasmReady) return;
    window.wasm.SetGIStrength(v);
  },
  getGIStrength() {
    if (!window.wasmReady) return 1.0;
    return window.wasm.GetGIStrength();
  },

  setMaterialColor(r, g, b) {
    if (!window.wasmReady) return;
    window.wasm.SetMaterialColor(r, g, b);
  },
  getMaterialColor() {
    var color = [0.0, 0.0, 0.0];
    if (!window.wasmReady) return color;
    var offset = window.wasm.GetMaterialColor();
    color[0] = window.wasm.getValue(offset, 'float');
    color[1] = window.wasm.getValue(offset + 4, 'float');
    color[2] = window.wasm.getValue(offset + 8, 'float');
    return {
      r: color[0] * 255,
      g: color[1] * 255,
      b: color[2] * 255
    };
  },
  setHairMaterialColor(r, g, b) {
    if (!window.wasmReady) return;
    window.wasm.SetHairMaterialColor(r, g, b);
  },
  getHairMaterialColor() {
    var color = [0.0, 0.0, 0.0];
    if (!window.wasmReady) return color;
    var offset = window.wasm.GetHairMaterialColor();
    color[0] = window.wasm.getValue(offset, 'float');
    color[1] = window.wasm.getValue(offset + 4, 'float');
    color[2] = window.wasm.getValue(offset + 8, 'float');
    return {
      r: color[0] * 255,
      g: color[1] * 255,
      b: color[2] * 255
    };
  },

  toggleLock() {
    if (!window.wasmReady) return false;
    var enabled = window.wasm.GetLocked() === 0 ? 1 : 0;
    window.wasm.Lock(enabled);
    return enabled;
  },


  toggleGrid() {
    if (!window.wasmReady) return true;
    var hidden = window.wasm.GetGridLines();
    window.wasm.SetGridLines(hidden);
    return hidden === 0 ? 1 : 0;
  },

  toggleSky() {
    if (!window.wasmReady) return true;
    var enabled = window.wasm.GetSky() === 0 ? 1 : 0;
    window.wasm.SetSky(enabled);
    return enabled;
  },
  
  toggleUnderwear() {
    if (!window.wasmReady) return true;
    var enabled = window.wasm.GetUnderwear() === 0 ? 1 : 0;
    window.wasm.SetUnderwear(enabled);
    return enabled;
  }

};

window.poserCanvasOnClick = PoserManager.canvasOnClick;

export default PoserManager;