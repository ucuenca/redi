'use strict';

/* Converts a string to CamelCase representation. If the 'strict' argument is given, converts to lower case first. */
wkhomeApp.filter('camelCase', function() {
  return function(input, isStrict) {
    if (!input || (isStrict && typeof(isStrict) !== 'boolean')) {
      return;
    }
    if (isStrict) {
      input = input.toLowerCase();
    }
    var words = input.split(/[ ,]+/);
    for (var i = 0, len = words.length; i < len; i++)
      words[i] = words[i].charAt(0).toUpperCase() + words[i].slice(1);
    return words.join(' ');
  };
});
