// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
        ["I lived in New York for 4 months",
         "Sometimes, I need to eat 2 portions of food to be full",
         "I like playing poker",
         "I once ripped off my entire fingernail while doing weights",
         "I did not travel overseas at all from 2008 to 2018",
         "My favourite dessert is chocolate lava cake",
         "I'm interested in relativity and quantum physics",
         "My favourite cuisin is Jap food",
         "My favourite pokemon is Snorlax",
         "My first meal of the year on 1st Jan is usually McDonalds supper after countdown"]

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * Adds a young photo
 */
function addBabyPhoto() {
    const imgUrl = 'images/baby_picture.JPG';
    const imgElement = document.createElement('img');
    imgElement.src = imgUrl;
    imgElement.style = 'height: 100%; width: 100%; object-fit: contain; padding-top: 1em;';

    const babyPhotoContainer = document.getElementById('picture-container');
    babyPhotoContainer.innerHTML = '';
    babyPhotoContainer.appendChild(imgElement);
}

/**
 * Removes the young photo
 */
function removeBabyPhoto() {
    const babyPhotoContainer = document.getElementById('picture-container');
    babyPhotoContainer.innerHTML = '';
}

/**
 * Fetches content from Java Servlet
 */
async function fetchContentFromServlet() {
    const response = await fetch('/data');
    const arr = await response.json();

    const stringList = document.getElementById('week2-fetch');

    stringList.innerHTML = '';
    stringList.appendChild(
        createListElement(arr[0]));
    stringList.appendChild(
        createListElement(arr[1]));
    stringList.appendChild(
        createListElement(arr[2]));
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

