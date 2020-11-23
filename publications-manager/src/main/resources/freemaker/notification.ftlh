<!DOCTYPE html>
	<html>
	    <head>
	      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	    </head>
	    <body>
	      <p>Hola ${name},</p>
	      <p>REDI ha agregado nuevas publicaciones de tu perfil de REDI </p>
        <p>Puedes acceder a tu perfil REDI en el siguiente enlace </p>
        <p><a href="${URI}" target="_blank">${name}</a></p>
        <p><br/></p>
        <p>Lista de nuevas publicaciones agregadas:</p>
        <ul>
          <#list lsPapers as item>
            <li><a href="${item.URL}" target="_blank">${item.title}</a></li>
          </#list>
        </ul>

	      <p>Saludos,</p>
	      <p>
	        <em>REDI</em> <br />
	      </p>
	    </body>
	</html>