/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

import {PolymerElement,html} from '@polymer/polymer/polymer-element.js';

class RightPanel extends PolymerElement {

    static get template() {
        return html`
            <!--content here-->
        `;
    }
}

customElements.define('right-panel', RightPanel);