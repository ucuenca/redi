/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* CORE MANAGEMENT 
 * Author Fernando Baculima
 * CEDIA 
 * 
 * */
/*
 * Get Publications Data to  Providers Graph
 */
function runGeneralPublication(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/publications_provider_graph",
    //    url:  "http://localhost:8079/marmotta/authors-module/update",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });

}

function runIndexacionPublications(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/indexing",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });

}

/*
 * Enrich author profile with attributes extracted from providers.
 */
function authorsAttr(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/author_attr",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });

}

/*
 * Get publications Data:   Provider Graph to General Publications Graph
 */

function runUpdatePublication(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var update = document.getElementById('updateScopus').checked;
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/publications?update=" + update,
    //    url:  "http://localhost:8079/marmotta/authors-module/update",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });

}

function runUpdatePublicationGOOGLE(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var update = document.getElementById('updateGoogleScholar').checked;
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/publications_google?update=" + update,
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });

}


function runUpdatePublicationDBLP(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/publications_dblp",
    //    url:  "http://localhost:8079/marmotta/authors-module/update",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });

}
function runUpdatePublicationMA(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/publications_ma",
    //    url:  "http://localhost:8079/marmotta/authors-module/update",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });

}


function runUpdatePublicationAK(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/publications_ak",
    //    url:  "http://localhost:8079/marmotta/authors-module/update",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });

}


/**
 * Method count the number publications in each graph and add this count to graph  khuskaCounters.
 * @author Freddy Sumba
 * @param {options} options
 */
function runPublicationsCount(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/count_publications_graph",
    //    url:  "http://localhost:8079/marmotta/authors-module/update",
    success: function (Result) {

      var start = new Date().getTime();
      for (var i = 0; i < 1e7; i++) {
        if ((new Date().getTime() - start) > 3000) {
          break;
        }
      }
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      //document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });

}

/**
 * Method that gets the publications from Dspace
 * @author Jose Luis Cullcay
 * @param {options} options
 */
function runUpdatePublicationDspace(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/publications_dspace",
    //    url:  "http://localhost:8079/marmotta/authors-module/update",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });
}
/**
 * Method that detect Latindex journals
 * @author José Ortiz
 * @param {options} options
 */
function runUpdateLatindex(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/publications_latindex",
    //    url:  "http://localhost:8079/marmotta/authors-module/update",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });
}
/**
 * Disambiguation Process
 * @author José Ortiz
 * @param {options} options
 */
function runUpdateDisambiguation(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/publications_disambiguation",
    //    url:  "http://localhost:8079/marmotta/authors-module/update",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });
}

/**
 * Central Graph Process
 * @author José Ortiz
 * @param {options} options
 */
function runUpdateCreateCentralGraph(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/publications_centralgraph",
    //    url:  "http://localhost:8079/marmotta/authors-module/update",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });
}


/**
 * Central Graph Process
 * @author José Ortiz
 * @param {options} options
 */
function runSyncMGDB(options) {

  document.getElementById("imgloading").style.visibility = "visible";

  var endpoint = "http://example";
  var graphuri = "http://example/data";
  var settings = {
    host: options
  }
  var dataT = {
    "Endpoint": endpoint,
    "GraphUri": graphuri
  };

  $.ajax({
    type: "POST",
    data: JSON.stringify(dataT),
    dataType: "text", //result data type
    contentType: "application/json", // send data type
    url: settings.host + "pubman/publications_sync",
    //    url:  "http://localhost:8079/marmotta/authors-module/update",
    success: function (Result) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert(Result);
    },
    error: function (data) {
      document.getElementById("imgloading").style.visibility = "hidden";
      alert("Error" + data.responseText);
    }
  });
}


function requestUploadFile(url, data, mime, callbacks) {
  function createRequest() {
    var request = null;
    if (window.XMLHttpRequest) {
      request = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
      request = new ActiveXObject("Microsoft.XMLHTTP");
    } else {
      throw "request object can not be created"
    }
    return request;
  }
  var request = createRequest();
  request.onreadystatechange = function () {
    if (request.readyState == 4) {
      if (callbacks.hasOwnProperty(request.status)) {
        callbacks[request.status](request.responseText, request);
      }
    }
  };
  request.open('POST', url, true);
  request.setRequestHeader("Content-Type", mime);
  request.send(data);
}

function runUploadBanner(options) {
  var file_id = $("#banner_id").val();
  var ins_ser = options + 'populate/uploadBanner?id=' + encodeURIComponent(file_id);
  var file = $("input#upload_file");
  var data = file.get(0).files[0];
  requestUploadFile(ins_ser, data, data.type, {
    200: function (resp, err) {
      alert("Image imported sucessfully.");
    }
  }
  );

}