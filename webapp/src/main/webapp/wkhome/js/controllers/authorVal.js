wkhomeControllers.controller('authorVal', ['$rootScope', '$scope', 'cookies', '$routeParams', '$window', 'globalData', 'profileval', 'saveprofile', 'searchTextResultsService', 'AuthorsService', 'Statistics', 'getProfile', 'querySubcluster',
  function ($rootScope, $scope, cookies, $routeParams, $window, globalData, profileval, saveprofile, searchTextResultsService, AuthorsService, Statistics, getProfile, querySubcluster) {
    // Define a new author object
    //$window.location.reload();
    var author = $routeParams.authorId;
    var orcid = "";
    var atk = "";
    var showbuttons = false;
    var neworg = false;
    // var newProf = author.includes("new");

    


    $scope.areasList = [];

    $scope.areaCombo = {};

    Statistics.query({
      id: 'keywords_frequencypub_gt4'
    }, function (data) {
      $scope.relatedtags = [];
      _.map(data["@graph"], function (keyword) {
        $scope.relatedtags.push({
          id: keyword["@id"],
          tag: keyword["rdfs:label"]["@value"]
        });
      });
    });

////////////////////////////////


//    $('#upload_file_btn').click(
    uploadImg = function () {
      var ins_ser = globalData.serverInstance + 'profileval/uploadPhoto?orcid=' + encodeURIComponent($scope.orcid);
      var file = $("input#upload_file");
      var data = file.get(0).files[0];
      requestUploadFile(ins_ser, data, data.type, {
        0: function (resp, err) {
          alert('Error al cargar imagen... Usar solo imagenes JPEG');
        },
        200: function (resp, err) {
          var rnd = Math.random();
          $scope.auth_img = globalData.serverInstance + 'profileval/getPhoto?orcid=' + encodeURIComponent($scope.orcid) + '&rnd=' + rnd;
          $scope.img = $scope.auth_img;
          alert("Image imported sucessfully.");
          $('#phto').attr('src', $scope.img);
        }
      }
      );
    };
//    );


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
          } else {
            callbacks[0](request.responseText, request);
          }
        }
      };
      request.open('POST', url, true);
      request.setRequestHeader("Content-Type", mime);
      request.send(data);
    }










/////////////////////////////////

    $scope.subareaCombo = {};

    $scope.area_indx = 1;

    addsubCluster = function () {
      if ($scope.subareaCombo.selected) {
        $scope.area_indx *= 0.99;
        activetable.row.add(['<input id="' + $scope.subareaCombo.selected.id + '" class="subclusters"  type="checkbox" name="selection" value="' + $scope.subareaCombo.selected.id + '" ' + 'checked' + '>',
          $scope.subareaCombo.selected.tag,
          $scope.subareaCombo.selected.id.toUpperCase(),
          $scope.area_indx
        ]).draw(false);
        alert('Área de investigación agregada');
      }
    };

    addCluster = function () {
      if ($scope.areaCombo.selected) {
        $scope.area_indx *= 0.99;
        $scope.areasList.push($scope.areaCombo.selected.id);
        activetable.row.add(['<input id="' + $scope.areaCombo.selected.id + '" class="clusters"  type="checkbox" name="selection" value="' + $scope.areaCombo.selected.id + '" ' + 'checked' + '>',
          $scope.areaCombo.selected.tag,
          $scope.areaCombo.selected.id.toUpperCase(),
          $scope.area_indx
        ]).draw(false);
        alert('Área de conocimiento agregada');
      }
    };


    Statistics.query({
      id: 'barchar'
    }, function (data) {
      // $scope.orgs = data;

      var pdata = _.map(data['@graph'], function (d) {
        return {"id": d['@id'], "name": d['uc:name']};
      });
      $scope.orgs = pdata;

    }, function (some) {
      console.log("Sacar org" + some);
    });


    changeAction = function (select) {
      //console.log ( select);
      if (select == "Other") {
        //  $('#endpoint_org').prop("disabled", true);
        // $('#newOrg').css("display", "inline");
        $scope.neworg = " ";
        //neworg = true ;
      } else {
        $scope.neworg = "";
        // $('#newOrg').css("display", "none");

      }
    }


    function Candidate(id, val, desc, path) {
      this.id = id;
      this.val = val;
      this.desc = desc;
      this.path = path;
    }

    //cookies.set ( globalData.getSession() , '{ "name" : "Saquicela Victor" , "firstName" :"Victor" , "lastName": "Saquicela" , "email": "vsaquicela@ucuenca.edu.ec" , "orcid" : "0234-45687" , "access_token" : "789545645621213"}');
    //var logg = cookies.get(globalData.getSession());

    var logg = globalData.getSession();
    console.log(logg);
    var lo = logg !== undefined && logg !== null && logg !== '';
    if (lo) {
      $scope.name = logg.family_name + ", " + logg.given_name;
      $scope.fname = logg.given_name;
      $scope.lname = logg.family_name;
      $scope.email = logg.email;
      $scope.orcid = "";
      $scope.atk = logg['redi_token'];
      $scope.bio = "";
    }
    $scope.img = "http://asoclinic.com/wp-content/uploads/2018/02/placeholder-face-big.png";

    orcid = $scope.orcid;
    atk = $scope.atk;


    function newAuthor() {
      //console.log ($scope.orcid ==  $routeParams.authorId);
      return  newProf;
    }


    function proccessResult(result) {
      console.log("Procesando Datos");
      if (result.response.docs.length > 0) {

        waitingDialog.hide();
        var authors = result.response.docs;
        if (authors.length > 1) {
          var path = "/author/profile/";
          var candidates = _.map(authors, function (author) {
            var id = author["lmf.uri"];
            var name = _.max(author.name, function (name) {
              return name.length;
            });
            var topics = _.chain(author.topics)
                    .uniq()
                    .first(10)
                    .value()
                    .join(", ");
            var candidate = new Candidate(id, name, topics, path);

            return candidate;
          });
          $scope.candidates = candidates;
          //searchTextResultsService.saveData(candidates , false);
          searchTextResultsService.saveData(candidates, $scope.orcid, true);
          $('#searchResults').modal('show');
        }
      }
    }




    function candidateAuthors() {
      AuthorsService.get({
        search: $scope.name
      }, function (result) {
        proccessResult(result);
      }, function (some) {
        console.log("Que llega?");
        console.log(some);
        alert("Problema al cargar los datos");
        // alert ("Problemas al cargar los datos");
        //    waitingDialog.hide();
        // var res = '{"responseHeader":{"status":0,"QTime":42,"params":{"q":"name:(Saquicela Victor)","fl":"lmf.uri,name,topics","fq":"org:*","wt":"json"}},"response":{"numFound":62,"start":0,"docs":[{"lmf.uri":"https://redi.cedia.edu.ec/resource/authors/UCUENCA/ojs/SAQUICELA_GALARZA__VICTOR_HUGO","name":["v saquicela","Saquicela-Galarza, V.","Squicela, Víctor","SAQUICELA GALARZA ,  VICTOR HUGO","victor saquicela","V., Saquicela","SAQUICELA GALARZA ,   VICTOR HUGO","Saquicela , Victor","Saquicela,Victor","V. Saquicela-Galarza","V Saquicela-Galarza","Victor, Saquicela","Saquicela, Víctor","victor squicela","V Saguicela","Victor Saquicela","Victor Saquicela Saquicela","Saquicela, Victor","Víctor Squicela","Victor Hugo Saquicela","Victor Hugo Saquicela Galarza","Vıctor Saquicela","V Saquicela","Víctor Saquicela","Víctor Hugo Saquicela","Victor Saquicela Galarza","V., Saquicela-Galarza","Víctor Saquicela Galarza","Víctor Hugo Saquicela Galarza","Saquicela Galarza , V","Saquicela , Víctor","Saquicela Galarza, Víctor Hugo","Víctor Saquicelak","Víctor, Saquicela","Victor, Saquicela-Galarza","Víctor, Saquicela-Galarza","Saquicela, V","Saquicela, V."],"topics":["Bioengineering","Bioengineering","Control and Optimization","Control and Optimization","Library and Information Sciences","Library and Information Sciences","Linkeddata","Linkeddata","Computer Vision and Pattern Recognition","Computer Vision and Pattern Recognition","Hardware and Architecture","Hardware and Architecture","Mathematics","Mathematics","Computer Networks and Communications","Computer Networks and Communications","Computers in Earth Sciences","Computers in Earth Sciences","Safety, Risk, Reliability and Quality","Safety, Risk, Reliability and Quality","semantic annotations","semantic annotations","Information Systems and Management","Information Systems and Management","Data Integration","Data Integration","Biomedical Engineering","Biomedical Engineering","Engineering","Engineering","Electrical and Electronic Engineering","Electrical and Electronic Engineering","Mathematics (all)","Mathematics (all)","Civil and Structural Engineering","Civil and Structural Engineering","Computer Science Applications","Computer Science Applications","Education","Education","Theoretical Computer Science","Theoretical Computer Science","Electronic, Optical and Magnetic Materials","Electronic, Optical and Magnetic Materials","Computer Science","Computer Science","Earth-Surface Processes","Earth-Surface Processes","Condensed Matter Physics","Condensed Matter Physics","Modeling and Simulation","Modeling and Simulation","Artificial Intelligence","Artificial Intelligence","Computer Science (all)","Computer Science (all)","Geography, Planning and Development","Geography, Planning and Development","COMPUTER SCIENCE, INFORMATION SYSTEMS","COMPUTER SCIENCE, INFORMATION SYSTEMS","Information Systems","Information Systems","Health Informatics","Health Informatics","Computer Graphics and Computer-Aided Design","Computer Graphics and Computer-Aided Design","Human-Computer Interaction","Human-Computer Interaction","Signal Processing","Signal Processing","Control and Systems Engineering","Control and Systems Engineering","Semantic Web","Semantic Web","Software","Software","Applied Mathematics","Applied Mathematics","Web Services","Web Services"]},{"lmf.uri":"https://redi.cedia.edu.ec/resource/authors/UPS/oai-pmh/SAQUICELA__FELIPE","name":["felipe saquicela","Saquicela, Felipe"]},{"lmf.uri":"https://redi.cedia.edu.ec/resource/authors/UCE/ojs/SANMARTIN__VICTOR","name":["victor sanmartin","Sanmartín, Victor"]},{"lmf.uri":"https://redi.cedia.edu.ec/resource/authors/UTMATCH/file/CHIMARRO_CHIPANTIZA__VICTOR_LEWIS","name":["Victor, Chimarro","CHIMARRO CHIPANTIZA, VICTOR LEWIS","victor chimarro"],"topics":["Physics and Astronomy","Physics and Astronomy","Physics and Astronomy (all)","Physics and Astronomy (all)"]},{"lmf.uri":"https://redi.cedia.edu.ec/resource/authors/UCE/ojs/VACA__VICTOR_HUGO","name":["Vaca, Victor Hugo","victor hugo vaca"]},{"lmf.uri":"https://redi.cedia.edu.ec/resource/authors/UCE/ojs/LOZADA_LOZADA__VICTOR","name":["Lozada Lozada, Victor","victor lozada lozada"]},{"lmf.uri":"https://redi.cedia.edu.ec/resource/authors/UPS/oai-pmh/OREJUELA__VICTOR_H","name":["Orejuela, Victor H"]},{"lmf.uri":"https://redi.cedia.edu.ec/resource/authors/FLACSO/oai-pmh/TORRES__VICTOR_H","name":["Torres, Victor H"]},{"lmf.uri":"https://redi.cedia.edu.ec/resource/authors/FLACSO/oai-pmh/TORRRES__VICTOR_HUGO","name":["Torrres, Victor Hugo"]},{"lmf.uri":"https://redi.cedia.edu.ec/resource/authors/FLACSO/oai-pmh/FAJNZYLBER__VICTOR","name":["Fajnzylber, Victor"]}]}}';

        // proccessResult ( JSON.parse(res));
        // console.log ($scope.orcid ==  $routeParams.authorId);

      });
    }

    function checkProfile(author, orcid) {
      getProfile.query({'id': author, 'orcid': orcid}, function (data) {
        console.log("REDIR");

        $window.location.hash = '/author/profileval/' + data.uri;
        $window.location.reload();
        //  $route.reload();
        console.log(data);
      }, function (some) {
        // console.log ("!!NoExiste");
        candidateAuthors();
      });
    }


    function loadProfile(author, orcid) {
      getProfile.query({'id': author, 'orcid': orcid}, function (data) {

        $scope.name = data.name;
        $scope.fname = data.fname;
        $scope.lname = data.lname;
        $scope.email = data.email;
        $scope.bio = data.bio;

        //onsole.log ($scope.orgs );
        // $scope.orgs.push ({ id : data.org , name : data.org });
        //  console.log ($scope.orgs );
        if (data.org.includes("http")) {
          $scope.org = data.org;
        } else {
          $scope.org = "Other";
          $scope.neworg = data.org;
        }
        /* if ($("#endpoint_org option[value='"+data.org+"']").val() === undefined){
         console.log ("NORECON");
         console.log (data.org); 
         $scope.org = data.org;
         $('#endpoint_org option[value="Other"]').prop('selected', true);
         $('#newOrg').prop('value', data.org ) ;
         $('#newOrg').css("display", "inline");
         } else {
         console.log ("RECON");
         $scope.org =  data.org;
         console.log ($scope.org);
         }*/
      }, function (some) {
        console.log("Problemas al cargar los datos");
      });

    }


    if (author == "_") {

      checkProfile(author, orcid);

      // console.log ("!!PORQ");


    } else if (author == "new_") {

      /* $(".showbuttons").removeClass("active");
       $('#PagDataSave').addClass("active");*/
      loadProfile(author, orcid);
      showbuttons = true;
    } else {
      showData(author, orcid);
      /*  $(".showbuttons").removeClass("active");
       $('#PagDataVal').addClass("active");*/
      showbuttons = false;
    }



    function showData(author, orcid) {

      waitingDialog.show("Cargando información, espere por favor");

      profileval.query({'id': author, 'orcid': orcid}, function (data) {
        waitingDialog.hide();
        var alldata = [];

        if ('img' in data.basic.data[0]) {
          $scope.img = data.basic.data[0].img;
        }
        //  $scope.img = data.basic.data[0].img;

        $scope.org = data.basic.data[0].org;

        loadProfile(author, orcid);
        $('#endpoint_org option[value="' + $scope.org + '"]').prop('selected', true);
        var tabla = rendertable(data.profiles.data);
        var tabla2 = rendertable2(data.names.data);
        var tabla3 = rendertable3(data.emails.data);
        var tabla4 = rendertable4(data.publications.data);
        for (var j = 0; j < data.clusters.data.length; j++) {
          var sss = data.clusters.data[j];
          $scope.areasList.push(sss["cluster"]);
          sss['score'] = 1;
        }
        var tabla5 = rendertable5(data.clusters.data);
        for (var j = 0; j < data.subclusters.data.length; j++) {
          var sss = data.subclusters.data[j];
          sss['score'] = 1;
        }
        var tabla6 = rendertable6(data.subclusters.data);
        var datasave = [];
        // rendertable2 (dataSet)
        alldata.push({"sec": "seczero", "nametable": "", "table": "", "data": ""},
                {"sec": "secone", "nametable": "profiles", "table": tabla, "data": datasave},
                {"sec": "sectwo", "nametable": "names", "table": tabla2, "data": datasave},
                {"sec": "secthree", "nametable": "emails", "table": tabla3, "data": datasave},
                {"sec": "secfour", "nametable": "publications", "table": tabla4, "data": datasave},
                {"sec": "secfive", "nametable": "clusters", "table": tabla5, "data": datasave},
                {"sec": "secsix", "nametable": "subclusters", "table": tabla6, "data": datasave});
        pagination(alldata);
      }, function (some) {

        alert("Problemas al cargar los datos");
        waitingDialog.hide();
        $("#PagDataVal").css("display", "none");
        $("#PagDataSave").css("display", "none");
      });
    }



    saveprof = function () {
      console.log("Almacenando datos");
      if (profile.org == "none") {
        alert("Se requiere que su perfil este asociada a una institución");
        return;
      }
      saveprofile.querySrv({'profile': JSON.stringify(profile), 'data': JSON.stringify(dataforsave), 'id': orcid, 'uri': author, 'atk': atk}, function (data) {

        alert("Datos almacenados. Gracias por su colaboración");
        $('#exampleModal').modal('hide');
        author = author == "new_" ? "" : author;
        $window.location.hash = '/author/profile/' + author;

      }, function (some) {
        // console.log ("Que llega?");
        console.log(some);
        waitingDialog.hide();
      });

    }

    function recoverprof() {
      profile.name = $scope.name;
      profile.img = $scope.img;
      profile.fname = $('#inputfname').val();
      profile.lname = $('#inputlname').val();
      profile.email = $('#inputemail').val();
      profile.bio = $('#biotext').val();
      profile.org = $('#endpoint_org').val();
      if (profile.org === "Other") {
        profile.org = $('#newOrg').val();
      }

    }

    saveOnlyProfile = function () {
      recoverprof();
      $('#exampleModal').modal();
      //saveprof ();
    }


    checkall = function (param) {

      if ($("input:checkbox#maincheckbox." + param).prop("checked")) {
        $("input:checkbox." + param + ":not(#maincheckbox)").prop("checked", true);

      } else {
        $("input:checkbox." + param + ":not(#maincheckbox)").prop("checked", false);

      }

    }

    function rendertable(dataSet) {

      table = $('#profileval1').DataTable({
        "dom": "lfrti",
        "lengthChange": false,
        "ordering": false,
        "info": true,
        "searching": false,
        "pageLength": 5,
        data: dataSet,
        columns: [
          {title: '<input onclick="checkall(\'profiles\')" id ="maincheckbox" class="profiles" type="checkbox" name="selection" >'},
          {data: "uri"},
          {data: "name"},
          {data: "subject"},
        ], columnDefs: [
          {
            "render": function (data, type, row) {
              return data;
            },
            targets: 1},
          {"render": function (data, type, row) {
              return data;
            }, targets: 2},
          {"render": function (data, type, row) {
              return   data ? data.toLowerCase().substring(0, 100) + "..." : "";
            }, targets: 3},
          {
            "render": function (data, type, row) {

              var status = "";
              if (row["status"]) {
                status = "checked";
              }
              return '<input id="' + row["uri"] + '" class="profiles" type="checkbox" name="selection" value="' + row["uri"] + '"  ' + status + '>';
            },
            targets: 0}


        ]
      });
      return table;
    }

    function rendertable2(dataSet) {
      table2 = $('#profileval2').DataTable({
        "dom": "lfrti",
        "lengthChange": false,
        "ordering": false,
        "info": true,
        "searching": false,
        "pageLength": 5,
        data: dataSet,
        columns: [
          {title: '<input onclick="checkall(\'names\')" id ="maincheckbox" class="names" type="checkbox" name="selection" >'},
          {data: "name"},
          {data: "other"}
        ], columnDefs: [
          {
            "render": function (data, type, row) {

              return data;
            },
            targets: 1},
          {"render": function (data, type, row) {
              return  data;
            }, targets: 2},
          {
            "render": function (data, type, row) {
              var status = "";
              if (row["status"]) {
                status = "checked";
              }
              return '<input id="' + row["name"] + '" class="names" type="checkbox" name="selection" value="' + row["name"] + '" ' + status + '>';
            },
            targets: 0}


        ]
      });
      return table2;
    }


    function rendertable3(dataSet) {
      table3 = $('#profileval3').DataTable({
        "dom": "lfrti",
        "lengthChange": false,
        "ordering": false,
        "info": true,
        "searching": false,
        "pageLength": 5,
        data: dataSet,
        columns: [
          {title: '<input onclick="checkall(\'emails\')" id ="maincheckbox" class="emails" type="checkbox" name="selection" >'},
          {data: "mail"}
        ], columnDefs: [
          {
            "render": function (data, type, row) {

              return data;
            },
            targets: 1},
          {
            "render": function (data, type, row) {
              var status = "";
              if (row["status"]) {
                status = "checked";
              }
              return '<input id="' + row["mail"] + '" class="emails"  type="checkbox" name="selection" value="' + row["mail"] + '" ' + status + '>';
            },
            targets: 0}


        ]
      });
      return table3;
    }

    function rendertable4(dataSet) {
      table4 = $('#profileval4').DataTable({
        "dom": "lfrti",
        "lengthChange": false,
        "ordering": false,
        "info": true,
        "searching": false,
        "pageLength": 5,
        data: dataSet,
        columns: [
          {title: '<input onclick="checkall(\'publications\')" id ="maincheckbox" class="publications" type="checkbox" name="selection" >'},
          {data: "title"},
          {data: "authors"}
        ], columnDefs: [
          {
            "render": function (data, type, row) {

              return data.split(";")[0];
            },
            targets: 1},
          {
            "render": function (data, type, row) {

              return data.toUpperCase();
            },
            targets: 2},
          {
            "render": function (data, type, row) {
              var status = "";
              if (row["status"]) {
                status = "checked";
              }
              return '<input id="' + row["uri"] + '" class="publications"  type="checkbox" name="selection" value="' + row["uri"] + '" ' + status + '>';
            },
            targets: 0}


        ]
      });
      return table4;
    }

    function rendertable5(dataSet) {
      table4 = $('#profileval5').DataTable({
        "dom": "lfrti",
        "lengthChange": false,
        "ordering": true,
        "order": [[3, "asc"]],
        "info": true,
        "searching": false,
        "pageLength": 5,
        data: dataSet,
        columns: [
          {title: '<input onclick="checkall(\'clusters\')" id ="maincheckbox" class="clusters" type="checkbox" name="selection" >'},
          {data: "label"},
          {data: "cluster"},
          {data: "score"}
        ], columnDefs: [
          {
            "render": function (data, type, row) {
              if (data) {
                return "<span style='display: none;'>" + data + "</span>";
              } else {
                return "<span style='display: none;'>" + row[3] + "</span>";
              }
            },
            targets: 3},
          {
            "render": function (data, type, row) {
              if (data) {
                return data.split(";")[0];
              } else {
                return row[1];
              }
            },
            targets: 1},
          {
            "render": function (data, type, row) {
              if (data) {
                return data.toUpperCase();
              } else {
                return row[2];
              }
            },
            targets: 2},
          {
            "render": function (data, type, row) {
              if (row["cluster"]) {
                var status = "";
                if (row["status"]) {
                  status = "checked";
                }
                return '<input id="' + row["cluster"] + '" class="clusters"  type="checkbox" name="selection" value="' + row["cluster"] + '" ' + status + '>';
              } else {
                return row[0];
              }
            },
            targets: 0}


        ]
      });
      return table4;
    }

    function rendertable6(dataSet) {
      table4 = $('#profileval6').DataTable({
        "dom": "lfrti",
        "lengthChange": false,
        "ordering": true,
        "order": [[3, "asc"]],
        "info": true,
        "searching": false,
        "pageLength": 5,
        data: dataSet,
        columns: [
          {title: '<input onclick="checkall(\'subclusters\')" id ="maincheckbox" class="subclusters" type="checkbox" name="selection" >'},
          {data: "label"},
          {data: "subcluster"},
          {data: "score"}
        ], columnDefs: [
          {
            "render": function (data, type, row) {
              if (data) {
                return "<span style='display: none;'>" + data + "</span>";
              } else {
                return "<span style='display: none;'>" + row[3] + "</span>";
              }
            },
            targets: 3},
          {
            "render": function (data, type, row) {
              if (data) {
                return data.split(";")[0];
              } else {
                return row[1];
              }
            },
            targets: 1},
          {
            "render": function (data, type, row) {
              if (data) {
                return data.toUpperCase();
              } else {
                return row[2];
              }
            },
            targets: 2},
          {
            "render": function (data, type, row) {
              if (row["subcluster"]) {
                var status = "";
                if (row["status"]) {
                  status = "checked";
                }
                return '<input id="' + row["subcluster"] + '" class="subclusters"  type="checkbox" name="selection" value="' + row["subcluster"] + '" ' + status + '>';
              } else {
                return row[0];
              }
            },
            targets: 0}


        ]
      });
      return table4;
    }

    var i = 0;
    var activetable;
    var dataforsave = {'profiles': [], 'names': [], 'emails': [], 'publications': [], 'clusters': [], 'subclusters': []};
    var profile = {'name': '', 'fname': '', 'lname': '', 'email': '', 'org': ''};
    function pagination(alldata) {

      //  var alldata = [{ "sec": "secone", "table": table } , { "sec": "sectwo", "table": table2 } , { "sec": "secthree", "table": table3 } , { "sec": "secfour", "table": table4 }]; 
      activetable = alldata[0].table;
      i = 0;



      next = function () {
        if (i == 0) {
          recoverprof();
          i = showsection(i, true, alldata);
        } else {
          //console.log (table.page.info());
          //  console.log (table.page());
          $("input:checkbox#maincheckbox." + alldata[i].nametable).prop('checked', false);
          dataforsave[alldata[i].nametable].push(savepag(alldata[i]));

          if ((activetable.page.info().page + 1) < activetable.page.info().pages) {
            activetable.page('next').draw('page');
          } else if (i > 0)
          {
            i = showsection(i, true, alldata);

          }
        }

      }


      prev = function () {
        //$('#prevButtonTable').on('click', function () {
        //    console.log (page());

        //console.log (dataforsave);
        if (activetable.page.info().page == 0) {
          i = showsection(i, false, alldata);

        } else {
          activetable.page('previous').draw('page');

        }
        dataforsave[alldata[i].nametable].pop();


        //});
      }





    }

    function showsection(i, next, alldata) {

      if (next && i < alldata.length - 1) {

        i++;
        var sec = alldata[i].sec;
        $(".sectionval").removeClass("active");
        $('#' + sec).addClass("active");
        activetable = alldata[i].table;

      } else if (next && i == alldata.length - 1) {
        //  alert ("Gracias por participar. En unos dias sus datos seran actualizados");

        $('#exampleModal').modal();
        // var senddata = {"objeto":dataforsave };

      } else if (i > 0) {
        i--;
        var sec = alldata[i].sec;
        $(".sectionval").removeClass("active");
        $('#' + sec).addClass("active");
        activetable = alldata[i].table;

      }

      if (activetable) {
        if (alldata[i].nametable == 'subclusters') {
          querySubcluster.query({
            id: _.uniq($scope.areasList).join(';')
          }, function (data) {
            var data = data.data;
            $scope.relatedtags2 = [];
            for (var j = 0; j < data.length; j++) {
              var adata = data [j].subclusters;
              for (var x = 0; x < adata.length; x++) {
                var kdata = adata[x];
                $scope.relatedtags2.push({
                  id: kdata["uri"],
                  tag: kdata["label-en"]
                });
              }
            }
          });
        }
      }
      return i;
    }
    function savepag(tables) {
      var proftable = $("input:checkbox." + tables.nametable);
      return recoverstatus(proftable);
    }




    $scope.showbuttonDespl = function () {
      return showbuttons;
    }



    function recoverstatus(datatable) {
      var data = [];
      _.each(datatable, function (a) {
        data.push({'id': a.value, 'status': a.checked});
      });
      return data;
    }
  }]);

