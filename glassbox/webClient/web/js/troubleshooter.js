/** include - including .js files from JS - bfults@gmail.com - 2005-02-09    **
 ** Code licensed under Creative Commons Attribution-ShareAlike License      **
 ** http://creativecommons.org/licenses/by-sa/2.0/                           **/
var hIncludes = null;
function include(sURI)
{
  if (document.getElementsByTagName)
  {
    if (!hIncludes)
    {
      hIncludes = {};
      var cScripts = document.getElementsByTagName("script");
      for (var i=0,len=cScripts.length; i < len; i++)
        if (cScripts[i].src) hIncludes[cScripts[i].src] = true;
    }
    if (!hIncludes[sURI])
    {
      var oNew = document.createElement("script");
      oNew.type = "text/javascript";
      oNew.src = sURI;
      hIncludes[sURI]=true;
      document.getElementsByTagName("head")[0].appendChild(oNew);
    }
  }
}

include("js/Base.js");
include("js/Logging.js");
include("dwr/engine.js");
include("dwr/util.js");
include("js/overlib.js");
include("js/overlib_crossframe.js");

var timerID = 0;
var tStart  = null;
var timeout = 2500;
var updateFunc;

/*********************************************************************
*Creates a timer.
*
*/
function UpdateTimer() {
   if(timerID) {
      clearTimeout(timerID);
   }   
   //update the page.
   updateFunc();
      
   timerID = setTimeout("UpdateTimer()", timeout);
}

/*********************************************************************
*Starts the timer
*
*/
function startTimer(timeoutArg, updateFuncArg) {
   tStart     = new Date();
   timeout    = timeoutArg;
   timerID    = setTimeout("UpdateTimer()", timeout);
   updateFunc = updateFuncArg;
}

/*********************************************************************
*Hijacks the event handler.
*
*/
function errorHandler(error) {
    //Do nothing..
    logError(error);
}


/*********************************************************************
*Callback for the row Ajax call.
*
*/
function loadRowInfo(data) {
  try {     
     updateRows(data);
  } catch (error) {
     logError(error);
  }
}

function showBox(id) {
    var editBox = $(id);
    editBox.style.height = '100%';
    editBox.style.width  =  '100%';
    editBox.style.visibility = 'visible';
}

function hideBox(id) {
    var editBox = $(id);
    editBox.style.visibility = 'hidden';
    editBox.style.height = '0px';
    editBox.style.width  =  '0px';
}


function showHint(obj, text) {
   var hint = document.getElementById("hint");
   hint.style.visibility="visible";
   hint.style.position="absolute";
   hint.style.left = obj.offsetLeft + 25 + "px";
   hint.style.top = obj.offsetTop + 5 + "px";
   DWRUtil.setValue("hintText", text);  
}

function hideHint(obj) {
  var hint = document.getElementById("hint");
   hint.style.visibility="hidden";
}

var detect = navigator.userAgent.toLowerCase();
var OS;
var browser;
var version;
var total;
var thestring;

if (checkIt('konqueror'))
{
	browser = "Konqueror";
	OS = "Linux";
}
else if (checkIt('safari')) browser = "Safari"
else if (checkIt('omniweb')) browser = "OmniWeb"
else if (checkIt('opera')) browser = "Opera"
else if (checkIt('webtv')) browser = "WebTV";
else if (checkIt('icab')) browser = "iCab"
else if (checkIt('msie')) browser = "Internet Explorer"
else if (!checkIt('compatible'))
{
	browser = "Netscape Navigator"
	version = detect.charAt(8);
}
else browser = "An unknown browser";

if (!version) version = detect.charAt(place + thestring.length);

if (!OS)
{
	if (checkIt('linux')) OS = "Linux";
	else if (checkIt('x11')) OS = "Unix";
	else if (checkIt('mac')) OS = "Mac"
	else if (checkIt('win')) OS = "Windows"
	else OS = "an unknown operating system";
}

function checkIt(string)
{
	place = detect.indexOf(string) + 1;
	thestring = string;
	return place;
}

function getEventX(event) {
 if(browser == 'Internet Explorer')
     return event.clientX
 else
     return event.clientX;
}

function getEventY(event) {
 if(browser == 'Internet Explorer')
     return event.clientY
 else
     return event.clientY;
}

function configureHint(name, text) {
    var obj = $(name);
    obj.onmouseover = function(event) {return overlib(text);};
    obj.onmouseout = function(event) {return nd();};	
}


function capitalizeMe(obj) {
    val = obj;
    newVal = '';
    val = val.split(' ');
    for(var c=0; c < val.length; c++) {
        newVal += val[c].substring(0,1).toUpperCase() + val[c].substring(1,val[c].length) + ' ';
    }
    return newVal;
}

function showDetails(tableid, startIndex) {
   var tbody = document.getElementById(tableid);
   for(var i = startIndex + 2; i < tbody.rows.length; i++) { 
        var row = tbody.rows[i];
	 if(row.style.display == 'none') {
	    var clickImage = document.getElementById("image-" + tableid);
	    clickImage.src="images/less-detail16.jpg";    
	    if(navigator.userAgent.indexOf('MSIE') != -1)
		row.style.display = 'block';
	    else
               row.style.display = 'table-row';
	 } else {
	     row.style.display = 'none';
	     var clickImage = document.getElementById("image-" + tableid);
	     clickImage.src="images/more-detail16.jpg";
	 }
    }
}

/** Run the email paradigm....**/
function email() {

}


/** Email popup control **/
function showEmail(event) {  
    document.getElementById('n1').style.visibility="visible";
    document.getElementById('n1').style.left=event.clientX
    document.getElementById('n1').style.top=event.clientY;
}

function hideEmail(event) {
    document.getElementById('n1').style.visibility="hidden";
}