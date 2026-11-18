import React, { Component } from 'react';
import ItemCard from './ItemCard';

import anime_kyo_female from '../imgs/character_models/anime_kyo_female.jpg';
import anime_kyo_male from '../imgs/character_models/anime_kyo_male.jpg';

import anime_girl from '../imgs/character_models/anime_girl.jpg';
import female_1_7 from '../imgs/character_models/1_7_female.jpg';
import female_new from '../imgs/character_models/female_new.jpg';
import man from '../imgs/character_models/man.jpg';

import PropTypes from 'prop-types';

import { Trans } from '@lingui/macro';

export default class ModelList extends Component {

  render() {
    return (
      <div className="list">
        <ItemCard
          poserFunc={this.props.onClickFunc}
          name={<Trans>1:6 Anime Girl</Trans>}
          src='anime_kyo_female'
          img={anime_kyo_female}>
        </ItemCard>
        <ItemCard
          poserFunc={this.props.onClickFunc}
          name={<Trans>1:6 Anime Boy</Trans>}
          src='anime_kyo_male'
          img={anime_kyo_male}>
        </ItemCard>
        <ItemCard
          poserFunc={this.props.onClickFunc}
          name={<Trans>Woman</Trans>}
          src='female_new'
          img={female_new}>
        </ItemCard>
        <ItemCard
          poserFunc={this.props.onClickFunc}
          name={<Trans>Man</Trans>}
          src='man'
          img={man}>
        </ItemCard>
        <ItemCard
          poserFunc={this.props.onClickFunc}
          name={<Trans>1:7 Woman</Trans>}
          src='female_1_7'
          img={female_1_7}>
        </ItemCard>
        <ItemCard
          poserFunc={this.props.onClickFunc}
          name={<Trans>1:5 Girl</Trans>}
          src='anime_girl'
          img={anime_girl}>
        </ItemCard>
      </div>
    )
  }
}

ModelList.propTypes = {
  onClickFunc: PropTypes.func.isRequired
};