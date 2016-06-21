pmm_plotter = function() {
	
	/**
	 * @author Markus Freitag, EITCO GmbH, MFreitag@eitco.de, 2015
	 * 
	 * CODE CONVENTIONS
	 * Please try to avoid native JavaScript for the creation of DOM elements. 
	 * Use jQuery for the sake of clarity whenever possible. Improvements of 
	 * code readability are welcome.
	 * 
	 * - Global variables are marked with an underscore prefix ("_") or as messages ("msg")
	 * - Functions that are only used once are nested in the closest scope.
	 * - Functions are roughly ordered in the order of first usage.
	 * 
	 * GENERAL INFO
	 * The function Math.log10 as well as log10 in general is not supported by IE browsers 
	 * (same applies to the KNIME built-in browser engine). For some models, this may result in
	 * strange behavior like a missing tip on mouseover. However, all graphs could be drawn 
	 * correctly so far.
	 */

	var modelPlotter = {
			version: "2.0.0"
	};
	modelPlotter.name = "Model Plotter";
	
	var _plotterValue;
	//	var plotterRep; // not used
	
	var _modelObjects = [];
	var _modelObjectsTemp = []; // for temporarily stored models like data points
	var _colorsArray = [];
	var _rawModels = [];
	var _dbUnits = [];
	var _parameterMap = [];
	var _recentPlot;
	var _COLORS = [			// copied frum function plot
	     'steelblue',
	     'red',
	     '#05b378',      // green
	     'orange',
	     '#4040e8',      // purple
	     'brown',
	     'magenta',
	     'cyan'
	];
	var msgAdd = "Add Model";
	var msgChoose = "Select Model";
	var msgTime = "Time";
	var msgNoMatrix = "no matrix data provided";
	var msgNoParameter = "no parameter provided";
	var msgNoName = "no name found";
	var msgNoFunction = "no function provided";
	var msgNoScore = "no quality score provided";
	var msgNext = "Next";
	var msg_To_ = " to ";
	var msgNoType = "No Type";
	var msgDone = "Done";
	var msgReportName = "Report Name";
	var msgAuthorsNames = "Authors";
	var msgComment = "Comment";
	var msgUnknown = "unknown";
	var msgIn = " in ";
	var msgName = "Name";
	var msgScore = "Quality Score";
	var msgFunction = "Function";
	var msgParameter = "Initial Parameters";
	var msgMatrix = "Matrix";
	var msgExamples = "Examples";
	var msgShowData = "show given data points";
	var msg_error_noFormulaSec = "ERROR: Formula in secondary model is not a valid formula.";
	var	msg_error_unknownUnit = "unknown unit: ";
	var	msg_error_xUnitUnknown = "the x unit of one function is unknown to the database: transformation impossible";
	var	msg_error_yUnitUnknown = "the y unit of one function is unknown or has no conversion factor in the database: transformation impossible";
	var msg_error_noConversionFunctionFactor = "The PMM-Lab database does not know the unit provided by the model - model cannot be added. Unit: "
	
	var _internalId = 0;
	var _xUnit = msgUnknown;
	var _yUnit = msgUnknown;
	
	/* the following values are subject to change */
	var _buttonWidth = "width: 250px;"; // not only used for buttons
	var _sliderWidth = "width: 190px;";
	var _sliderInputWidth = "width: 40px;";
	var _sliderBoxHeight = "height: 33px;";
	var _sliderStepSize = 0.0001; // aligns perfectly with the input field size
	var _totalHeight = "height: 800px;";
	var _plotWidth = 600;
	var _plotHeight = 300;
	var _defaultFadeTime = 500; // ms
	var _defaultTimeout = 200; // ms // responsiveness (lower) vs. performance/fluence (higher)
	
	modelPlotter.init = function(representation, value) {
		
		// view bug - require js prevents global variable behavior 
		if (!window.functionPlot) {
			window.functionPlot = parent.KnimePageLoader.getLibrary("de/bund/bfr/knime/pmm/js/modelplotter/function-plot");
		}
		
		_plotterValue = value;
		_rawModels = value.models.schemas;
		_dbUnits = value.units.units;
		// plotterRep = representation; // not used

		initLayout();
		initData();
		initJQuery();
	};
	
	/**
	 * initializes data that is necessary from the very beginning (models to select)
	 */
	function initData() 
	{
		// parse models and create selection menu
		addSelectOptions();
	}
	
	/**
	 * initializes all layout elements, e.g. calls jQuery methods to create jQuery 
	 * objects from the DOM elements
	 */
	function initJQuery() 
	{
		$("#dataChoiceDiv").hide();
		
		// make buttons jquery buttons
		$("#nextButton").button({
			icons: {
				primary: "ui-icon-arrow-1-e"
			},
			disabled: true
		});
		
		$("#addModelButton").button({
			icons: {
				primary: "ui-icon-plus"
			},
			disabled: true
		}).click( function () 
			{
				// once a model is added, we can activate the "next" button
				$("#nextButton").button( "option", "disabled", false );
				$("#dataChoiceDiv").show();
			}
		);
		
		$("#dataChoiceCheckbox").click( function() {
			if(isDataPointsCheckboxChecked())
				plotDataPoints()
			else
				unplotDataPoints()
		});
		
		// make the selection a jquery select menu
		$("#modelSelectionMenu").selectmenu({
			change: function () {
				$("#addModelButton").button( "option", "disabled", false );
			}
		});
		
		// setup the div for the meta section as jQuery accordion
		$("#metaDataWrapper").accordion({
			content: "height-style",
			collapsible: true
		});
		
		$("#dataChoiceSelect").selectmenu();
	}
	
	/**
	 * initalizes and style all DOM elements, divs and placeholders
	 */
	function initLayout()
	{
		/*
		 * body
		 */
		var body = document.getElementsByTagName("body")[0];
		$('body').css({
			"width": "100%",
			"height": "100%",
			"background": "#fdfdfd",
			"font-family": "Verdana,Helvetica,sans-serif",
			"font-size": "12px",
			"overflow": "hidden"
		});
		
		/*
		 * layout
		 */
		var layoutWrapper = document.createElement("div");
		layoutWrapper.setAttribute("id", "layoutWrapper");
		layoutWrapper.setAttribute("style", "width: 1000px;");
		body.appendChild(layoutWrapper);
		
		// left Pane
		var leftWrapper = document.createElement("div");
		leftWrapper.setAttribute("id", "leftWrapper");
		leftWrapper.setAttribute("style", "width: 300px; display: block; float: left;" + _totalHeight);
		layoutWrapper.appendChild(leftWrapper);		

		// selection
		var modelSelectionMenu = document.createElement("select");
		modelSelectionMenu.innerHTML = msgChoose;
		modelSelectionMenu.setAttribute("id", "modelSelectionMenu");
		modelSelectionMenu.setAttribute("style" , _buttonWidth);
		leftWrapper.appendChild(modelSelectionMenu);
		
		// inactive selection option serves both as a text hint for the user and a as placeholder
		var selectionPlaceholder = document.createElement("option");
		selectionPlaceholder.setAttribute("hidden", true);
		selectionPlaceholder.setAttribute("disabled", true);
		selectionPlaceholder.setAttribute("selected", true);
		selectionPlaceholder.setAttribute("value", "");
		selectionPlaceholder.innerHTML = msgChoose;
		modelSelectionMenu.appendChild(selectionPlaceholder);
		
		// add button
		var addModelButton = document.createElement("button");
		addModelButton.innerHTML = msgAdd;
		addModelButton.setAttribute("id", "addModelButton");
		addModelButton.setAttribute("style", _buttonWidth + "margin-bottom: 3px;");
		addModelButton.addEventListener("click", function() { 
				addFunctionFromSelection(); 
		});
		leftWrapper.appendChild(addModelButton);
		
		// slider wrapper
		var sliderWrapper = document.createElement("div");
		sliderWrapper.setAttribute("id", "sliderWrapper");
		sliderWrapper.setAttribute("style" , _buttonWidth);
		leftWrapper.appendChild(sliderWrapper);
		
		var nextButton = $("<button>", {
			id: "nextButton", 
			style: _buttonWidth, 
			text: msgNext 
		});
		$("#leftWrapper").append(nextButton);
		nextButton.on("click", function() 
			{ 
				$("#layoutWrapper").fadeOut(_defaultFadeTime, function() {
					showInputForm();
				});
			}
		);
		
		// right pane
		var rightWrapper = document.createElement("div");
		rightWrapper.setAttribute("id", "rightWrapper");
		rightWrapper.setAttribute("style", "width: 600px; display: block; float: left;" + _totalHeight);
		layoutWrapper.appendChild(rightWrapper);
		
		// div that includes the plotted functions
		var plotterWrapper = document.createElement("div");
		plotterWrapper.setAttribute("id", "plotterWrapper");
		rightWrapper.appendChild(plotterWrapper);
		
		// div for data choice buttons
		var dataChoiceDiv = document.createElement("div");
		dataChoiceDiv.setAttribute("style", "width: 200px; height: 40px;");
		dataChoiceDiv.setAttribute("id", "dataChoiceDiv");
		rightWrapper.appendChild(dataChoiceDiv);
		
		$("#dataChoiceDiv").append(
			$('<form><input type="checkbox" id="dataChoiceCheckbox" value="showTestData" checked />' + msgShowData + '</form>')
		);
		
		// meta data
		var metaDataWrapper = document.createElement("div");
		metaDataWrapper.setAttribute("id", "metaDataWrapper");
		rightWrapper.appendChild(metaDataWrapper);
	}
	
	/**
	 * chooses models to add to the selection menu and triggers adding
	 * 
	 * @param modelsArray list of models decoded by the node
	 */
	function addSelectOptions(modelsArray)
	{	
		idList = []; // used to make sure there is only one option per global model id
		if(_rawModels)
		{
			$.each(_rawModels, function(i) 
				{
					var dbuuid = _rawModels[i].dbuuid;
					// only add if not added before
					if(idList.indexOf(dbuuid) == -1)
					{
						var type = _rawModels[i].matrix.name;
						var modelName = _rawModels[i].estModel.name;
						// pass data
						addSelectOption(dbuuid, modelName, type);
						// remember id
						idList.push(dbuuid);
					}
				}
			);
		}
	}
	
	/**
	 * adds a new option to the selection menu
	 * options of the same type are grouped (group name is type)
	 * options with no type have the "no type" group
	 * 
	 * @param dbuuid id of the model
	 * @param modelName name of the model
	 * @param type type of the model
	 */
	function addSelectOption(dbuuid, modelName, type)
	{
		if(!type || type == "")
			type = msgNoType;
		
		// html <option>
		var option = document.createElement("option");
		option.setAttribute("value", dbuuid);
		option.innerHTML = "[" + dbuuid + "] " + modelName;
		
		// find or create html <optgroup>
		var groupId = "optGroup_" + type;
		var group = document.getElementById(groupId);
		if(!group) 
		{
			var group = document.createElement("optgroup");
			group.setAttribute("id", groupId);
			group.setAttribute("label", type);
			document.getElementById("modelSelectionMenu").appendChild(group);
		}
		group.appendChild(option);
	}

	/**
	 * Add-Button event in three steps:
	 * 1. determines the selected model from the selection menu
	 * 2. gets the model data
	 * 3. calls addFunctionObject() with the model data
	 */
	function addFunctionFromSelection()
	{
		// get the selection
		var selectMenu = document.getElementById("modelSelectionMenu");
		var selection = selectMenu.options[selectMenu.selectedIndex].value;

		// get the model data
		var model;
		var modelList = [];
		
		// we do a primitive clone for the iteration over the original data 
		// (it helps to start fresh for each model)
		var rawDataClone = JSON.parse(JSON.stringify(_rawModels));
		
		$.each(rawDataClone, function(i, object)
		{
			if(object.dbuuid == selection)
			{
				modelList.push(object);
			}
		});
		
		if(modelList.length >= 1)
		{
			model = createTertiaryModel(modelList); // this has to be done first
			model.paramList.params.Y0 = _plotterValue.y0; // set the value from the settings here
			
			var dbuuid = model.dbuuid;
			var modelName = model.estModel.name;
			var functionAsString = prepareFunction(model.indeps, model.formula, model.xUnit, model.yUnit);
			var functionConstants = prepareParameters(model.indeps, dbuuid);
			// call subsequent method
			addFunctionObject(dbuuid, functionAsString, functionConstants, model);
		}
		
		/**
		 * [nested function]
		 * parse function formula from model and modify it according to framework needs
		 * 
		 * @param functionString formula as delivered by the java class
		 * @return parsed function 
		 */
		function prepareFunction(parameterArray, functionString, xUnit, yUnit) {

			var newString = functionString;
			
			// cut the left part of the formula
			if(newString.indexOf("=") != -1)
			{
				var leftSide = newString.split("=")[0];
				newString = newString.replace(leftSide + "=", "");
			}
			
			// replace "T" and "Time" with "x" using regex
			// gi: global, case-insensitive
			newString = newString.replace(/Time/gi, "x");
			newString = newString.replace(/\bT\b/gi, "x");
			
			if(_xUnit != msgUnknown && xUnit != _xUnit)
			{
				newString = unifyX(newString, xUnit);
			}
			else
			{
				_xUnit = xUnit;
			}
			
			if(_yUnit != msgUnknown && _yUnit != yUnit)
			{
				newString = unifyY(newString, yUnit);
			}
			else
			{
				_yUnit = yUnit;
			}
			
			// math.js does not know "ln", ln equals log
			newString = newString.replace(/\bln\b/gi, "log");
			
			/*
			 * replaces "expression^(0.5)" with "sqrt(expression)"
			 */
			newString = newString.replace(/\(([^)^()]+)\)\^\(0\.5\)/g, function(part) {
				part = part.replace("^(0.5)", "");
				part = "sqrt(" + part + ")";
				return part;
			});
			return newString;
		}

		/**
		 * [nested function]
		 * extract parameter names and values
		 * 
		 * @param parameterArray relevant parameters from the model
		 * @param modelId the id of the current model; it is used for the ranges
		 * @return reduced parameter array
		 */
		function prepareParameters(parameterArray, modelId) 
		{
			// this will be returned containing the preprocessed parameters
			var newParameterArray = {};
			
			$.each(parameterArray, function(index, param) {
				var paramName = param.name;
				var paramValue;
				if (param.value != undefined)
					paramValue = param.value;
				else if(param.min != undefined)
					paramValue = param.min;
				else paramValue = 0;
				
				newParameterArray[paramName] = paramValue; 
				
				// save ranges for each parameter
				var parameterData = {
					name: paramName,
					value: paramValue,
					model: modelId,
					min: param.min,
					max: param.max
				};
				
				var existent = false;
				
				$.each(_parameterMap, function(i, existingEntry) {
					if(existingEntry.name == parameterData.name)
					{
						// set value to existing value - new model will get current values
						// override initial value
						newParameterArray[paramName] = existingEntry.value
						// override global map
						parameterData.value = existingEntry.value;
						
						
						// extend existing ranges
						if(parameterData.min < existingEntry.min)
							existingEntry.min = parameterData.min;
						if(parameterData.max > existingEntry.max)
							existingEntry.max = parameterData.max;
						existent = true;
						
						return true;
					}
				});
				if(!existent)
					_parameterMap.push(parameterData);
			});
			return newParameterArray;
		}
		
		/**
		 * [nested function]
		 * use the primary and secondary models to create the tertiary model
		 * parses all nested formula and secondary parameters and injects them
		 * into the primary model (tertiary model)
		 * 
		 * @param modelList all models (data rows) that belong to the same model id
		 * @return tertiary model (if applicable)
		 */
		function createTertiaryModel(modelList)
		{
			/*
			 * we use the primary model data as a foundation for the tertiary model
			 * this applies to the attributes that are equal in all secondary models/data rows
			 */
			var tertiaryModel = modelList[0]; // primary model data is shared
			var formulaPrim = tertiaryModel.catalogModel.formula; // main formula is shared
			var paramsPrim = tertiaryModel.paramList.params; // so are the primary parameters
			var indepsPrim = tertiaryModel.indepList.indeps; // so are the primary parameters
			var depPrim = tertiaryModel.dep; // so are the primary parameters
			
			var secondaryIndeps = []; // gather the variable independents for the sliders
			
			// get the global xUnit from the model
			$.each(indepsPrim, function(i) {
				var currentIndep = indepsPrim[i];
				
				if(currentIndep["name"] == "Time" || currentIndep["name"] == "T")
				{
					var xName = currentIndep["unit"];
					tertiaryModel.xUnit = xName;
					return true;
				}
			});
			if(depPrim)
			{
				tertiaryModel.yUnit = depPrim.unit;
			}
			
			// add primary independents (which are in the parameters here)
			$.each(paramsPrim, function(index, indep) {
				// convention: if a parameter has no mininmum or maximum but a value, it shall not be dynamic
				if( (indep.min == undefined || indep.min == "" || indep.min == null) && 
					(indep.max == undefined || indep.max == "" || indep.max == null) &&
					(indep.value != undefined && indep.value !== ""))
				{
					var regex = new RegExp("\\b" + indep["name"] + "\\b", "gi");
					formulaPrim = formulaPrim.replace(regex, indep["value"]);
				}
				else
				{
					// default behavior
					secondaryIndeps.push(indep);
				}
			});
			
			if(tertiaryModel.m2List)
			{
				// extract secondary independents
				$.each(tertiaryModel.m2List.schemas, function(i1, modelSec) {
					var indepsSec = modelSec.indepList.indeps;
					$.each(indepsSec, function(i2, indep) {
						secondaryIndeps.push(indep);
					});
				});
				
				// extract and replace secondary parameters (constants)
				$.each(tertiaryModel.m2List.schemas, function(index, modelSec) {
					// in catModelSec, a formula is expected
					var formulaSec = modelSec.catalogModel.formula;
					// in paramsSec, the values for that formula are expected
					var paramsSec = modelSec.paramList.params;
	
					
					// we now simply replace the parameters from catModelSec with
					// the values from paramsSec
					if(paramsSec)
						$.each(paramsSec, function(index, param) {
							var regex = new RegExp("\\b" + param["name"] + "\\b", "gi");
							formulaSec = formulaSec.replace(regex, param["value"]);			 
						});
					modelSec.formula = formulaSec; // new field holds the flat formula
				});
	
				// inject nested formula in primary formula
				$.each(tertiaryModel.m2List.schemas, function(index, modelSec) {
					var formulaSecRaw = modelSec.formula;
					var formulaSec;
					var parameterPrim;  
					
					if(formulaSecRaw.indexOf("=") != -1)
					{
						// parameter name
						parameterPrim = formulaSecRaw.split("=")[0];
						// its formula from the secondary model
						formulaSec = formulaSecRaw.split("=")[1];
	
						/* 
						* we exchange the primary parameter with its formula from the secondary model
						* the parameter itself is computed depending on independents and cannot be 
						* changed directly therefore we remove it from the independents list of the 
						* tertiary model
						*/
						var indexToDelete;
						
						$.each(secondaryIndeps, function(index, indep){
							 if(indep["name"] == parameterPrim)
							 {
								 indexToDelete = index;
								 return true;
							 }
						});
						if(indexToDelete != undefined)
							secondaryIndeps.splice(indexToDelete, 1);
					}
					else
					{
						show(msg_error_noFormulaSec);
					}
					var regex = new RegExp("\\b" + parameterPrim + "\\b", "gi");
					formulaPrim = formulaPrim.replace(regex, "(" + formulaSec + ")");
				});
			}
			var points = [];
			var timeSeries = tertiaryModel.timeSeriesList.timeSeries;
			
			if(timeSeries)
			{
				$.each(timeSeries, function(i2, dataPointItem) {
					var point = [ 
						dataPointItem.time, 
						dataPointItem.concentration 
					];
					points.push(point);
				});
			}
			
			tertiaryModel.dataPoints = points;	
			
			/*
			* post check
			* if you want to rename parameters consistently for all upcoming actions,
			* do it here
			*/
			$.each(secondaryIndeps, function(index, indep){
				var oldName = indep.name;
				var newName = oldName;
				var category = indep.category
				
				if(category == "Temperature")
					newName = "Temperature";
				else if(category == "Time")
					newName = "Time";
				/* 
				 * special handling for indeps that are called "T" (sometimes used 
				 * for temperature), because "T" also sometimes refers to time and 
				 * will otherwise be exchanged with "x" in the formula
				 */
				else if(oldName == "T")
					newName = "T1";
				// else dont do anything
					
				var oldNamePattern = new RegExp("\\b" + oldName + "\\b", "gi");
				
				formulaPrim = formulaPrim.replace(oldNamePattern, newName);
				// renaming at last
				indep.name = newName;
			});
			tertiaryModel.formula = formulaPrim;
			tertiaryModel.indeps = secondaryIndeps;
			
			return tertiaryModel;
		}
	}
	
	/**
	 * @return whether the data point checkbox is checked
	 */
	function isDataPointsCheckboxChecked() {
		return $("#dataChoiceCheckbox").is(":checked");
	}
	
	/**
	 * adds a function to the functions array and redraws the plot
	 * 
	 * @param dbuuid
	 * @param functionAsString the function string as returend by prepareFunction()
	 * @param the function constants as an array 
	 */
	function addFunctionObject(dbuuid, functionAsString, functionConstants, model)
	{
		var color = getNextColor(); // functionPlot provides 9 colors
		var maxRange = _plotterValue.maxXAxis * 1000; // obligatoric for the range feature // TODO: dynamic maximum
		var range = [0, maxRange];
		var id = ++_internalId;
		
		var modelObj = { 
			 id: id,
             dbuuid: dbuuid,
			 fnType: 'linear',
			 name: model.estModel.name,
			 fn: functionAsString,
			 scope: functionConstants,
			 color: color,
			 range: range,
			 skipTip: false,
			 modelData: model
		};
		
		// for given data, we add an additional graph that only includes the data points
		if(model.dataPoints.length > 0)
		{
			var modelPointObj = {
				id: id,
				dbuuid: dbuuid,
				points: model.dataPoints,
			    color: color,
			    skipTip: false,
			    fnType: 'points',
			    graphType: 'scatter'
			};
			if(isDataPointsCheckboxChecked())
				_modelObjects.push(modelPointObj);
			else
				_modelObjectsTemp.push(modelPointObj);
		}
		
		// add model to the list of used models
		_modelObjects.push(modelObj);
		
		// create dom elements in the meta accordion
		addMetaData(modelObj);

		// update plot and sliders after adding new function
		updateParameterSliders();
		
		// redraw with all models
		drawD3Plot();
		
	}
	
	/**
	 * deletes a model for good - including graph and meta data
	 * 
	 * @param internalId dbuuid of the model
	 */
	function deleteFunctionObject(internalId)
	{
		deleteMetaDataSection(internalId);
		removeModel(internalId);
		updateParameterSliders();
		
		/* 
		 * if there are no models to show left, the user cannot continue to the next 
		 * page anymore
		 */
		if(_modelObjects.length == 0)
		{
			// disable button
			$("#nextButton").button( "option", "disabled", true);
			$("#dataChoiceDiv").hide();
			
			// reset variables
			_parameterMap = [];
			_xUnit = msgUnknown;
			_yUnit = msgUnknown;
		}
		
		drawD3Plot();
		
		/*
		 * [nested function]
		 * removes the model from the used model array
		 * 
		 * @param id dbuuid of the model
		 */
		function removeModel(id)
		{
			var reducedArray = [];
			$.each(_modelObjects, function (index, model) 
				{
					// if id and color equal, it is the model that is meant to be deleted
					if(model && model.id != id)
					{
						// only non-deleted models remain
						reducedArray.push(model)
					}
				}
			);
			_modelObjects = reducedArray;
		}
		
		/*
		 * [nested function]
		 * deletes the dom elements that belong to the meta data in the accordion
		 * 
		 * @param id dbuuid of the model
		 */
		function deleteMetaDataSection(id)
		{
			// remove meta data header
			var header = document.getElementById("h" + id);
			header.parentElement.removeChild(header);
			
			// remove meta data
			var data = document.getElementById(id);
			data.parentElement.removeChild(data);
			
			$("#metaDataWrapper").accordion("refresh");
		}
	}
	
	/**
	 * adds a new entry for a new model object and shows it in the accordion below the plot
	 * 
	 * @param modelObject the recently added modelObject
	 */
	function addMetaData(modelObject) 
	{
		/*
		 * Accordion needs a header followed by a div. We add a paragraph per parameter.
		 * The individual paragraph includes two divs, containing the parameter name and 
		 * value respectively.
		 * 
		 * Structure for each meta entry:
		 * > h3 (header)
		 * >> div (button>
		 * > div
		 * >> p
		 * >>> div (bold)
		 * >>> div
		 * >> p 
		 * >>> div /bold)
		 * >>> div
		 * ...
		 */
		var header = document.createElement("h3");
		header.setAttribute("id", "h" + modelObject.id);
		header.innerHTML = modelObject.dbuuid;
		
		// accordion-specific jQuery semantic for append()
		$("#metaDataWrapper").append(header);
		
		var deleteDiv = document.createElement("span");
		deleteDiv.setAttribute("style", "float: right; color: transparent; background: transparent; border: transparent;")
		header.appendChild(deleteDiv);
		
		var deleteButton = document.createElement("button");
	    $(deleteButton).button({
	        icons: {
	          primary: "ui-icon-closethick"
	        },
	        text: false
	    }).click(function(event) {
	    	// we use color as an additional identifier in case the same model was added more than once
	    	deleteFunctionObject(modelObject.id);
	    });
	    deleteButton.setAttribute("style", 	"color: transparent; background: transparent; border: transparent;");
		deleteDiv.appendChild(deleteButton);
		
		// color field
		var colorDiv = document.createElement("span");
		colorDiv.setAttribute("style", 
				"float: left; color: " + modelObject.color 
				+ "; background:  " + modelObject.color 
				+ "; border: 1px solid #cac3c3; margin-right: 5px; height: 10px; width: 10px; margin-top: 3px;")
		header.appendChild(colorDiv);

		var colorDivSub = document.createElement("button");
	    $(colorDivSub).button({
	        icons: {
	          primary: "ui-icon-blank"
	        },
	        text: false
	    });
		colorDivSub.setAttribute("style", 
				"float: left; color: " 
				+ modelObject.color + "; background: " 
				+ modelObject.color + "; border: 0px; height: 10px; width: 10px;")
		colorDiv.appendChild(colorDivSub);
		
		// meta content divs divs
		var metaDiv = document.createElement("div");
		metaDiv.setAttribute("id", modelObject.id);
		$("#metaDataWrapper").append(metaDiv);

		// name of the model
		addMetaParagraph(msgName, modelObject.name, msgNoName);
		// model formula (function)
		addMetaParagraph(msgScore, modelObject.modelData.estModel.qualityScore, msgNoScore);
		// matrix data
		addMetaParagraph(msgFunction, reparseFunction(modelObject.fn), msgNoFunction);
		// function parameter
		addMetaParagraph(msgParameter, unfoldScope(modelObject.scope), msgNoParameter);
		// quality score
		var matrix = modelObject.modelData.matrix;
		addMetaParagraph(msgMatrix, (matrix.name || "") + "; " + (matrix.detail || ""), msgNoMatrix);
		
		// ... add more paragraphs/attributes here ...
		
		// use jquery to refresh the accordion values
		$("#metaDataWrapper").accordion("refresh");
		
		var numSections = document.getElementById("metaDataWrapper").childNodes.length / 2;
		// open last index
		$("#metaDataWrapper").accordion({ active: (numSections - 1) });
		
		/**
		 * [nested function]
		 * adds a paragraph in the section for passed parameter data
		 * 
		 * @param title bold header title of the parameter (its name)
		 * @param content the value of the parameter
		 * @param alt alternative msg if parameter is null or empty
		 */
		function addMetaParagraph(title, content, alt) 
		{
			var header = "<div style='font-weight: bold; font-size:10px;'>" + title + "</div>";
			if(!content || content == "; ")
				var content = alt;
			var inner = "<div>" + content + "</div>";
			
			var paragraph = $("<p></p>").append(header, inner);
			$(metaDiv).append(paragraph);
		}
		
		/**
		 * [nested function]
		 * adapt formula for readability
		 * 
		 * @param formula function formula
		 */
		function reparseFunction(formula)
		{
			newFormula = formula.replace(/\bx\b/gi, msgTime);
			newFormula = newFormula.replace(/\blog\b/gi, "ln");
			return newFormula;
		}
		
		/**
		 * [nested function]
		 * parses the parameter array and creates a DOM list from its items
		 * 
		 * @param paramArray an array of key value pairs that contains the parameters and 
		 * their respective values
		 */
		function unfoldScope(paramArray)
		{
			if(!paramArray)
				return null;
			var list = "";
			$.each(paramArray, function(elem) 
				{
					list += ("<li>" + elem + ": " + paramArray[elem] + "</li>");
				}
			);
			var domElement = "<ul type='square'>" + list + "</ul>";
			return domElement;
		}
	}

    /**
     * adds, updates and removes sliders for all dynamic constants
     */
	function updateParameterSliders()
	{
	    var sliderWrapper = document.getElementById("sliderWrapper");
	    var sliderIds = []; // ids of all sliders that correspond to a constant from the used models
	    
	    // add or update sliders
	    for (var modelIndex in _modelObjects)
	    {
	    	var constants = _modelObjects[modelIndex].scope;
	    	if(constants)
	    	{
		    	$.each(constants, function(constant, value)
		    	{
					var sliderId = "slider_" + constant.toUpperCase();
					sliderIds.push(sliderId); // remember active sliders
					
					// do not recreate if already in the DOM
					if(document.getElementById(sliderId))
					{
						// do not add known parameters twice
						return true;
					}
					
					// determine slider range
					// standard values if no range given
					var sliderMin = value - 13.37;
					var sliderMax = value + 13.37;
					
					$.each(_parameterMap, function (index, range) {
						if(range.name == constant)
						{
							if(range.min != undefined)
								sliderMin = range.min;
							if(range.max != undefined)
								sliderMax = range.max;
						}
					});
					
					sliderMin = roundValue(sliderMin);
					sliderMax = roundValue(sliderMax);
					/*
					 * the layout structure is as follows:
					 * > sliderBox
					 * >> sliderLabel
					 * >> slider | >> sliderValueDiv
					 * 			   >>> sliderValueInput
					 */
				    var sliderBox = document.createElement("p");
				    sliderBox.setAttribute("id", sliderId);
				    sliderBox.setAttribute("style" , _buttonWidth + _sliderBoxHeight);
				    sliderWrapper.appendChild(sliderBox);
				    
					var sliderLabel = document.createElement("div");
					var labelText = "<b>" + constant + "</b>" + " (" + sliderMin + msg_To_ + sliderMax + ")";
					sliderLabel.innerHTML = labelText;
					sliderLabel.setAttribute("style" , "font-size: 10px;");
					sliderBox.appendChild(sliderLabel);
					
					var slider = document.createElement("div");
					slider.setAttribute("style" , _sliderWidth + "display: block; float: left; margin: 3px");
					sliderBox.appendChild(slider);
									    
					var sliderValueDiv = document.createElement("div");
					sliderValueDiv.setAttribute("style" , _sliderInputWidth + "display: block; float: left;");
					sliderBox.appendChild(sliderValueDiv);
					
					var sliderValueInput = document.createElement("input");
					sliderValueInput.setAttribute("type", "number");
					sliderValueInput.setAttribute("style" , _sliderInputWidth + "font-weight: bold;");
					sliderValueDiv.appendChild(sliderValueInput);
					
					sliderValueInput.setAttribute("min", sliderMin);
					sliderValueInput.setAttribute("max", sliderMax);
					
					// set input field to initial value
					$(sliderValueInput).val(value);
					
					// configure slider, its range and init value
				    $(slider).slider({
				    	value: value,
				    	min: sliderMin,
				    	max: sliderMax,
				    	step: _sliderStepSize,
				    	// changing the slider changes the input field
				        slide: function( event, ui ) {
				            $(sliderValueInput).val( ui.value );
				            // delay prevents excessive redrawing
				            window.setTimeout(updateFunctionParameter(constant, ui.value), _defaultTimeout);
				        }
				    });
					$(sliderValueInput).change(function() {
						// changing the input field changes the slider
						$(slider).slider("value", this.value);
							if(this.value == undefined || this.value == "")
								return;
							// delay prevents excessive redrawing
							window.setTimeout(updateFunctionParameter(constant, this.value), _defaultTimeout);
					});
					// react immediately on key input
					$(sliderValueInput).keyup(function() {
						$(this).change();
					});
				});
	    	}
	    }
	    
	    // at last, we delete unused sliders
	    var allIds = []; // ids of all shown sliders (may include obsolete sliders)
	    var sliderWrapperChildren = sliderWrapper.children;

	    for(var i = 0; i < sliderWrapperChildren.length; i++) 
	    {
	    	allIds.push(sliderWrapperChildren[i].id);
	    };

	    // delete obsolete sliders
	    $.each(allIds, function(i) {
	    	// check if slider is still used
	    	var found = sliderIds.indexOf(allIds[i]);
	    	// if not used, remove from DOM
	    	if(found == -1)
	    		sliderWrapper.removeChild(document.getElementById(allIds[i]));
	    });
	}

	/** 
	 * update a constant value in all functions
	 * 
	 * @param parameter parameter name
	 * @param value (new) parameter value
	 */
	function updateFunctionParameter(parameter, value)
	{
		var newValue = parseFloat(value);
		// update formula for all existing models
		for(var modelIndex in _modelObjects)
		{
			var constants = _modelObjects[modelIndex].scope;
			if(constants && constants[parameter] != undefined)
				constants[parameter] = newValue;
		}
		// update global map for future models
		$.each(_parameterMap, function(index, parameterEntry) {
			if(parameterEntry.name == parameter)
			{	
				parameterEntry.value = newValue;
			}
		});
		
		drawD3Plot();
	}

	/**
	 * redraws the plot and all graphs based on the modelObjects array and its data
	 */
	function drawD3Plot() 
	{
		// the plot element has to be reset because otherwise functionPlot may draw artifacts
		var d3Plot = document.getElementById("d3plotter");
		if(d3Plot)
		{
			d3Plot.parentElement.removeChild(d3Plot);
		}
		d3Plot = document.createElement("div");
		d3Plot.setAttribute("id", "d3plotter");
		
		var wrapper = document.getElementById("plotterWrapper");
		wrapper.appendChild(d3Plot);
		
		// plot
		try
		{
			functionPlot({
			    target: '#d3plotter',
			    xDomain: [-1, _plotterValue.maxXAxis],
			    yDomain: [-1, _plotterValue.maxYAxis],
			    xLabel: "Time" + msgIn + _xUnit,
			    yLabel: _yUnit,
			    height: _plotHeight,
			    width: _plotWidth,
			    tip: 
			    {
			    	xLine: true,    // dashed line parallel to y = 0
				    yLine: true,    // dashed line parallel to x = 0
				    renderer: function (x, y, index) {
				    	return roundValue(y);
					}
				},
			    data: _modelObjects
			});
		} 
		catch(e)
		{
			show(e);
		}

		window.setTimeout(serializePlot(), _defaultTimeout);
		
		function serializePlot() {
			var svgElement = document.getElementById("d3plotter").firstChild;
			var serializer = new XMLSerializer();
			if(svgElement != undefined && serializer != undefined)
			{
				_recentPlot = serializer.serializeToString(svgElement);
				_plotterValue.svgPlot = _recentPlot;
			}
		}
	}
	
	/**
	 * redraws the plot with data point functions
	 */
	function plotDataPoints()
	{
		$.merge(_modelObjects, _modelObjectsTemp)
		drawD3Plot();
	}
	
	/**
	 * redraws the plot without data point functions
	 */
	function unplotDataPoints()
	{
		var newArrayToPlot = [];
		$.each(_modelObjects, function(index, model) {
			if(model.fnType == 'points')
				_modelObjectsTemp.push(model);
			else
				newArrayToPlot.push(model);
		});
		_modelObjects = newArrayToPlot;
		drawD3Plot();
	}
	
	/**
	 * Deletes the view and opens a "second page" for the input of the user data.
	 * This function is meant to be called when the user has finished the plot.
	 */
	function showInputForm()
	{
		$("#layoutWrapper").empty();
		inputMember = [msgReportName, msgAuthorsNames, msgComment] // order matters
		
		var form = $("<form>", { 
			style: _buttonWidth + "; display: none;"
			
			});
		$.each(inputMember, function(i) {
			var paragraph = $("<p>", { style: _buttonWidth });
			var label = $("<div>", { 
				text: inputMember[i], 
				style: "font-weight: bold;  font-size: 10px;" + _buttonWidth 
			});
			var input = $('<input>', { 
				id: "input_" + inputMember[i].replace(/\s/g,""), 
				style: "width: 224px;" 
			})
			.button()
			.css({
			    'font' : 'inherit',
			    'background': '#eeeeee',
			    'color' : 'inherit',
			    'text-align' : 'left',
			    'outline' : 'thick',
			    'cursor' : 'text'
			});
			form.append(paragraph);
			paragraph.append(label);
			paragraph.append(input);
		})
		$(document.body).append(form);
		
		var finishButton = $("<button>", {
			id: "finishButton", 
			style: _buttonWidth + "; display: none;", 
			text: msgDone 
		}).button();
		
		$("#input_" + inputMember[0].replace(/\s/g,"")).val(_plotterValue.reportName);
		$("#input_" + inputMember[1].replace(/\s/g,"")).val(_plotterValue.authors);
		$("#input_" + inputMember[2].replace(/\s/g,"")).val(_plotterValue.comments);
		
		finishButton.on("click", function() 
			{ 
				_plotterValue.reportName = $("#input_" + inputMember[0].replace(/\s/g,"")).val();
				_plotterValue.authors = $("#input_" + inputMember[1].replace(/\s/g,"")).val();
				_plotterValue.comments = $("#input_" + inputMember[2].replace(/\s/g,"")).val();
				
				$(document.body).fadeOut(_defaultFadeTime);
			}
		);
		$(document.body).append(finishButton);
		$(form).fadeIn(_defaultFadeTime);
		$(finishButton).fadeIn(_defaultFadeTime);
	}
	
	/**
	 * Searches the list of DB-units for a conversion factor of a specific unit.
	 * This factor is used to normalize a function value (undo scale).
	 * 
	 * @param unit the unit from which the factor has to be determined
	 * @return the conversion factor fo the given unit
	 */
	function getUnitConversionFactor(unit)
	{
		var factor;
		$.each(_dbUnits, function (i, dbUnit) {
			if(unit == dbUnit.displayInGuiAs)
			{
				factor = dbUnit.conversionFunctionFactor;
				return true;
			}
		});
		
		if(factor == undefined)
			show(msg_error_noConversionFunctionFactor + unit);
		
		return factor;
	}
	
	/**
	 * Searches the list of DB-units for an inverse conversion factor of a specific unit.
	 * This factor is used to adapt a normalized function to a unit.
	 * 
	 * @param unit the unit from which the factor has to be determined
	 * 
	 * @return the conversion factor fo the given unit
	 */
	function getUnitInverseConversionFactor(unit)
	{
		var factor;
		$.each(_dbUnits, function (i, dbUnit) {
			if(unit == dbUnit.displayInGuiAs)
			{
				factor = dbUnit.inverseConversionFunctionFactor;
				return true;
			}
		});
		return factor;
	}
	
	/**
	 * Rearranges formula to fit to a common xAxis. We assume here, 
	 * that time is either counted in minutes ("min"), days ("d") or 
	 * hours ("h").
	 * 
	 * @param oldFunction non-unified function
	 * @param xUnit unit of the model to the oldFunction
	 * 
	 * @return unified function (String)
	 */
	function unifyX(oldFunction, xUnit)
	{
		var newFunction;
		var modifier = "*1";
		var defaultUnit = _xUnit;
		var secondUnit = xUnit;
		var defaultFactor = getUnitConversionFactor(defaultUnit).split("*")[1];
		var secondFactor = getUnitConversionFactor(secondUnit).split("*")[1];
		
		if(defaultFactor == undefined || secondFactor == undefined)
		{
			show(msg_error_xUnitUnknown);
			return oldFunction;
		}
		
		if(defaultFactor > secondFactor)
			modifier = "*" + defaultFactor / secondFactor;
		else
			modifier = "/" + secondFactor / defaultFactor;
			
		newFunction = modifyX(oldFunction, modifier);
		return newFunction;
	}
	
	/**
	 * convert x of a formula according to a common scale unit
	 * 
	 * @param unmodified function
	 * @param modifier includes operator + number that modify x
	 * 
	 * @return converted function for x in hours
	 */
	function modifyX(oldFunction, modifier)
	{
		newFunction = oldFunction.replace(/\bx\b/gi, "(x" + modifier + ")");
		return newFunction;
	}
	
	/**
	 * Rearranges formula to fit to a common yAxis. We assume here, 
	 * that the unit is either given in ln or log10.
	 * 
	 * @param oldFunction non-unified function
	 * @param yUnit unit of the model to the oldFunction
	 * 
	 * @return unified function (String)
	 */
	function unifyY(oldFunction, yUnit)
	{
		var newFunction;
		var defaultUnit = _yUnit;
		var secondUnit = yUnit;
		
		var normalizationFactor = getUnitConversionFactor(secondUnit);
		var adaptionFactor = getUnitInverseConversionFactor(defaultUnit);
		
		if(normalizationFactor == undefined || adaptionFactor == undefined)
		{
			show(msg_error_yUnitUnknown);
			return oldFunction;
		}
		
		// normalize function
		newFunction = modifyY(oldFunction, normalizationFactor);
		// apply default scale
		newFunction = modifyY(newFunction, adaptionFactor);

		return newFunction;
	}
	
	/**
	 * Modifies a function to match the current value unit
	 * 
	 * @param oldFunction non-unified function
	 * @param modifier operation to perform
	 * 
	 * @return unified function (String)
	 */
	function modifyY(oldFunction, modifier)
	{
		var newFunction = modifier.replace(/\bx\b/gi, "(" + oldFunction + ")");
		return newFunction;
	}
	
	/**
	 * color iterator based on the colors delivered by functionPlot (10 colors)
	 * 
	 * @return a color value
	 */
	function getNextColor()
	{
		if(_colorsArray.length <= 0)
			_colorsArray = _COLORS.slice(0); // clone function plot colors array
		return _colorsArray.shift();
	}
	
	/**
	 *	maintenance function: prints the content of any object
	 */
	function show(obj)
	{
		alert(JSON.stringify(obj, null, 4));
	}
	
	/**
	 * rounds a decimal value to at most 2 places
	 * 
	 * @param value a decimal value
	 * 
	 * @return rounded value
	 */
	function roundValue(value)
	{
		var roundedValue = Math.round((value + 0.00001) * 100) /  100;
		return roundedValue;
	}

	/*
	 * mandatory for JS
	 */
	modelPlotter.validate = function() 
	{
		return true;
	}
	
	modelPlotter.setValidationError = function () 
	{ 
		show("validation error");
	}
	
	modelPlotter.getComponentValue = function() 
	{
	    return _plotterValue;
	}
	
	modelPlotter.getSVG = function()
	{
		return _recentPlot
	}
	
	/*
	 * KNIME
	 */
	if (parent !== undefined && parent.KnimePageLoader !== undefined)
	{
		parent.KnimePageLoader.autoResize(window, frameElement.id)
	}
	
	return modelPlotter;	
}();

