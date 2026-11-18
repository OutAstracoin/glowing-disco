import React, { Component } from 'react';
import { Button } from 'reactstrap';
import ItemCard from './ItemCard';

import PoserManager from '../PoserManager';

import nakedImg from '../imgs/hair/Naked.PNG';

import { Trans } from '@lingui/macro';

// const poseImgs = importAll(require.context('../imgs/hair', false, /\.(png|jpe?g|svg)$/));

// function importAll(r) {
//   return r.keys().map(r);
// }

const hairs = [
  // {
  //   img: require('../imgs/hair/Naked.PNG'),
  //   model: 'naked'
  // },
  {
    img: require('../imgs/hair/alice.png'),
    model: 'Rig_Alice_v2'
  },
  {
    img: require('../imgs/hair/Archer.png'),
    model: 'Rig_archer_v2'
  },
  // {
  //   img: require('../imgs/hair/ayase_eli.png'),
  //   model: ''
  // },
  {
    img: require('../imgs/hair/dongtiaoxi.png'),
    model: 'Rig_Dongtiaoxi_v2'
  },
  // {
  //   img: require('../imgs/hair/doutanuki.png'),
  //   model: ''
  // },
  {
    img: require('../imgs/hair/Dubianyao.PNG'),
    model: 'Rig_Dubianyao_v2'
  },
  {
    img: require('../imgs/hair/gzym.png'),
    model: 'gzym_v2'
  },
  {
    img: require('../imgs/hair/ikarosu.png'),
    model: 'Rig_ikarosu_v2'
  },
  {
    img: require('../imgs/hair/miku.png'),
    model: 'Rig_Miku_v2'
  },
  {
    img: require('../imgs/hair/Mio.PNG'),
    model: 'Rig_Mio_v2'
  },
  {
    img: require('../imgs/hair/naruto.png'),
    model: 'naruto_v2'
  },
  {
    img: require('../imgs/hair/Nico.png'),
    model: 'Rig_Nico_v2'
  },
  {
    img: require('../imgs/hair/Rem.PNG'),
    model: 'Rig_Rem_v2'
  },
  {
    img: require('../imgs/hair/sjwl.PNG'),
    model: 'sjwl_v2'
  },
  {
    img: require('../imgs/hair/tatara.PNG'),
    model: 'tatara_v2'
  },
  {
    img: require('../imgs/hair/umi.png'),
    model: 'Rig_Umi_v2'
  },
  {
    img: require('../imgs/hair/ysfx.PNG'),
    model: 'ysfx_v2'
  },
  {
    img: require('../imgs/hair/ysl.png'),
    model: 'ysl_v2'
  }
];



export default class HairMenu extends Component {
  // constructor(props) {
  //   super(props);
  //   this.toggle = this.toggle.bind(this);
  //   this.state = { unfold: -1 };
  // }

  // toggle() {
  //   this.props.toggleParent();
  // }

  render() {
    let hs = [];
    for (let i = 0, len = hairs.length; i < len; i++) {
      hs.push(
        <ItemCard 
          key={hairs[i].model}
          poserFunc={PoserManager.addHair}
          src={hairs[i].model}
          img={hairs[i].img}>
        </ItemCard>);
    }

    return (
      <div className="side-menu">
        <Button block>{<Trans>Hairs</Trans>}</Button>
        <div className="list">
          <ItemCard 
            key={'naked'}
            poserFunc={PoserManager.removeHair}
            src={0}
            img={nakedImg}>
          </ItemCard>
          {hs}
        </div>
      </div>
    );
  }
}