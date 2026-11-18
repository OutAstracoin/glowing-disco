import React, { Component } from 'react';

import App from './App';

import catalogEn from './locales/en/messages.js'
import catalogZh from './locales/zh/messages.js'
import catalogJa from './locales/ja/messages.js'
import { I18nProvider } from '@lingui/react';
import { WarningModal } from './WarningModal';

var getLocale = require('browser-locale');

const catalogs = { zh: catalogZh, ja: catalogJa, en: catalogEn };

export default class AppI18nWrapper extends Component {

  constructor(props) {
    super(props);

    this.updateLanguage = this.updateLanguage.bind(this);

    this.locale = getLocale();

    if (!(this.locale in catalogs)) {
      this.locale = this.locale.substring(0, 2);
      if (!(this.locale in catalogs)) {
        this.locale = 'en';
      }
    }

    
    console.log(this.locale);
  }


  updateLanguage(lang)
  {
    if (this.locale !== lang) {
      this.locale = lang;
      this.forceUpdate();
    }
  }

  render() {
    return (
    <I18nProvider language={this.locale} catalogs={catalogs}>
      <App updateLanguage={this.updateLanguage}/>
      <WarningModal></WarningModal>
    </I18nProvider>
    );
  }
};