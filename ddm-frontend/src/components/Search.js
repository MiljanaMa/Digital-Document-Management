import React, { useState } from "react";
import {Button, TextField, MenuItem} from '@mui/material';
import axiosInstance from '../config/AxiosConfig';

function Search() {

  const [searchType, setSearchType] = useState("basic");

  const [filters, setFilters] = useState({
    employeeFullName: "",
    incidentSeverity: "",
    affectedOrganizationName: "",
    securityOrganizationName: "",
    text: "",
    knn: false,
    address: "",
    radius: "",
    unit: "km",
    basic: "",
  });
  const booleanQueryOPERANDS = ['AND', 'OR', 'NOT']
  const booleanQueryFields = ['employeeFullName:', 'affectedOrganizationName:', 'securityOrganizationName:', 'incidentSeverity: niska', 'incidentSeverity: srednja', 'incidentSeverity: visoka', 'incidentSeverity: kriticna', 'content:' ]

  const [results, setResults] = useState([]);
  const [searchDTO, setSearchDTO] = useState("");
  const [loading, setLoading] = useState(false);

  function handleFilterChange(e) {
    const { name, value, type, checked } = e.target;
    setFilters((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  }

  async function handleSearch() {
    setLoading(true);
    let payload = {};

    // eslint-disable-next-line default-case
    switch (searchType) {
      case "basic":
        const tokenizedKeywords = tokenizeInput(filters.basic);
        const payload1 = { keywords: tokenizedKeywords };

        try {
            const response = await axiosInstance.post(
                `search/simple1?isKnn=${filters.knn}`,
                payload1
            );
            const data = response.data.content;
            setResults(data);
            setLoading(false);
        } catch(err) {
            console.log("Greska prilikom dobavljanja rezultata.", err);
            setLoading(false);
        }
        break;
      case "geo":
        payload = { ...filters };
        try{
            const response = await axiosInstance.post('search/simple', payload);
            const data = response.data.content
            setResults(data);
            setLoading(false);
        } catch(err){
            console.log("Greska prilikom dobavljanja rezultata.")
            setLoading(false);
        }
        break;
      case "advanced":
        payload = {
          keywords: parseBoolExpression(searchDTO)
        };
        try{
            const response = await axiosInstance.post('search/advanced', payload);
            const data = response.data.content
            setResults(data);
            setLoading(false);
        } catch(err){
            console.log("Greska prilikom dobavljanja rezultata.")
            setLoading(false);
        }
        break;
    }
  }
  function tokenizeInput(text) {
  const regex = /"([^"]+)"|(\S+)/g;
  const tokens = [];
  let match;

  while ((match = regex.exec(text)) !== null) {
    if (match[1]) {
      tokens.push(match[1]);
    } else if (match[2]) {
      tokens.push(match[2]);
    }
  }

  return tokens;
}
  const parseBoolExpression = (input) => {
    const tokens = [];
    const parts = input.trim().split(/\s+/);

    const OPERATORS = new Set(["AND", "OR", "NOT"]);

    let i = 0;
    while (i < parts.length) {
      const part = parts[i];

      if (OPERATORS.has(part.toUpperCase())) {
        tokens.push(part.toUpperCase());
        i++;
      } else if (part.includes(":")) {
        const [field, firstValuePart] = part.split(":", 2);
        let valueParts = [firstValuePart];

        i++;
        while (
          i < parts.length &&
          !OPERATORS.has(parts[i].toUpperCase()) &&
          !parts[i].includes(":")
        ) {
          valueParts.push(parts[i]);
          i++;
        }

        const fullValue = valueParts.join(" ");

        const hasQuotes = fullValue.startsWith('"') && fullValue.endsWith('"');

        if (hasQuotes) {
          tokens.push(`${field}:${fullValue}`);
        } else {
          tokens.push(`${field}:${fullValue}`);
        }
      } else {
        tokens.push(part);
        i++;
      }
    }
    console.log("=== Parsed tokens ===");
    tokens.forEach((t, idx) => {
      console.log(`${idx + 1}. ${t}`);
    });

    return tokens;
  };
  async function download(doc) {
    try {
        const response = await axiosInstance.get(`file/${doc.serverFilename}`, {
          responseType: 'blob'
        });

        const objectUrl = URL.createObjectURL(response.data);
        const a = document.createElement('a');
        a.href = objectUrl;
        a.download = doc.title || doc.serverFilename;

        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(objectUrl);
    } 
    catch(err){
        console.log("Greska prilikom dobavljanja rezultata.")
        setLoading(false);
    }
  }

  return (
    <div style={{ maxWidth: 1400, margin: "auto", padding: 20 }}>
      <h1>Pretraga</h1>

      {/* Izbor tipa pretrage */}
      <div style={{ marginBottom: 20 }}>
        <label>
          <input
            type="radio"
            name="searchType"
            value="basic"
            checked={searchType === "basic"}
            onChange={(e) => setSearchType(e.target.value)}
          />
          Basic Search
        </label>
        <label style={{ marginLeft: 20 }}>
        <input
            type="radio"
            name="searchType"
            value="geo"
            checked={searchType === "geo"}
            onChange={(e) => setSearchType(e.target.value)}
          />
          Geo Search
        </label>
        <label style={{ marginLeft: 20 }}>
          <input
            type="radio"
            name="searchType"
            value="advanced"
            checked={searchType === "advanced"}
            onChange={(e) => setSearchType(e.target.value)}
          />
          Advanced Search
        </label>
      </div>

      {/* Filteri */}
      <div style={{ background: "#f8faff", padding: 20, borderRadius: 6 }}>
        <div style={{ marginTop: 10, display: searchType === "basic" ? "block" : "none" }} >
            <TextField
                      label="Employee Full Name"
                      name="employeeFullName"
                      variant="filled"
                      sx={{ mb: 3, display: 'none' }}
                      style={{ marginRight: 10, width: 300 }}
                      value={filters.employeeFullName}
                      onChange={handleFilterChange}
                    />
            <TextField
                      label="Incident severity"
                      name="incidentSeverity"
                      variant="filled"
                      sx={{ mb: 3, display: 'none' }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.incidentSeverity}
                      onChange={handleFilterChange}
                    >
                <MenuItem value="niska">niska</MenuItem>
                <MenuItem value="srednja">srednja</MenuItem>
                <MenuItem value="visoka">visoka</MenuItem>
                <MenuItem value="kriticna">kriticna</MenuItem>
            </TextField>

          <TextField
                      label="Affected organization"
                      name="affectedOrganizationName"
                      variant="filled"
                      sx={{ mb: 3, display: 'none' }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.affectedOrganizationName}
                      onChange={handleFilterChange}
                    />
          <TextField
                      label="Security organization"
                      name="securityOrganizationName"
                      variant="filled"
                      sx={{ mb: 3, display: 'none' }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.securityOrganizationName}
                      onChange={handleFilterChange}
                    />       
            <div style={{ margin: '10px 0', alignItems: 'center' }}>
              <label style={{ marginLeft: 10, fontWeight: 'bold' }}>
                <input
                  type="checkbox"
                  name="knn"
                  checked={filters.knn}
                  onChange={handleFilterChange}
                  style={{ marginRight: 5 }}
                />
                KNN
              </label>
            </div>
            <TextField
                          label="Text"
                          name="basic"
                          variant="filled"
                          sx={{ mb: 3 }}
                          fullWidth
                          style={{ marginRight: 10, width: 1230}}
                          value={filters.basic}
                          onChange={handleFilterChange}
                        />

   
      </div>
      <div style={{ marginTop: 10, display: searchType === "geo" ? "block" : "none" }} >
      <TextField
                      label="Address"
                      name="address"
                      variant="filled"
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.adress}
                      onChange={handleFilterChange}
                    />
            <TextField
                      type="number"
                      label="Radius"
                      name="radius"
                      variant="filled"
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.radius}
                      onChange={handleFilterChange}
                    />
            <TextField
                      select
                      label="Unit"
                      name="unit"
                      variant="filled"
                      fullWidth
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 130  }}
                      value={filters.unit}
                      onChange={handleFilterChange}
                      required
                    >
                      <MenuItem value="m">Meter</MenuItem>
                      <MenuItem value="km">Kilometer</MenuItem>
                    </TextField>
      </div>
      <div style={{ marginTop: 10, display: searchType === "advanced" ? "block" : "none" }} >
            <TextField
                      name="fullText"
                      variant="filled"
                      sx={{ mb: 3 }}
                      fullWidth
                      style={{ marginRight: 10}}
                      value={searchDTO}
                      onChange={(e) => setSearchDTO(e.target.value)}
                    />
        {/* Dugmići za boolean operatore */}
        <div className='mt-2 flex gap-2 flex-wrap'>
          {booleanQueryOPERANDS.map((operand, index) => (
            <Button
              variant='outlined'
              size='small'
              key={index}
              onClick={() => {
                let newSearchDTO = searchDTO;
                newSearchDTO += ` ${operand} `;
                setSearchDTO(newSearchDTO);
              }}
            >
              {operand}
            </Button>
          ))}
        </div>

        {/* Dugmići za polja (fields) */}
        <div className='mt-2 flex gap-2 flex-wrap'>
          {booleanQueryFields.map((field, index) => (
            <Button
              variant='outlined'
              color='secondary'
              sx={{ textTransform: "none" }}
              size='small'
              key={index}
              onClick={() => {
                let newSearchDTO = searchDTO;
                newSearchDTO += ` ${field}`;
                setSearchDTO(newSearchDTO);
              }}
            >
              {field}
            </Button>
          ))}
        </div>
      </div>

      <Button variant="contained" color="primary"
        onClick={handleSearch}
        disabled={loading}
        style={{ marginTop: 15, padding: "8px 15px" }}>
        {loading ? "Pretražujem..." : "Search"}
      </Button>

      {/* Rezultati */}
      <div style={{ marginTop: 30 }}>
        <h2>{results.length} Results</h2>
        {results.map((r, i) => (
          <div
            key={i}
            style={{
              padding: 15,
              marginBottom: 15,
              border: "1px solid #ddd",
              borderRadius: 5,
              background: "white",
              display: "flex",
              flexDirection: "column",
            }}
          >
            <h3 style={{ color: "#3366cc", cursor: "pointer" }}>{r.index.title}</h3>
            <table
              style={{
                borderCollapse: "collapse",
                width: "auto",
                margin: "1rem auto",
                fontSize: "1rem",
                textAlign: "left"
              }}
            >
              <tbody>
                {[
                  { label: "Employee:", value: r.index.employeeFullName, field: "employeeFullName" },
                  { label: "Security organization:", value: r.index.securityOrganizationName, field: "securityOrganizationName" },
                  { label: "Affected organization:", value: r.index.affectedOrganizationName, field: "affectedOrganizationName" },
                  { label: "Affected organization address:", value: r.index.affectedOrganizationAddress, field: "affectedOrganizationAddress" },
                  { label: "Content:", value: r.index.content, field: "content" } // content uvek prikazan
                ].map((item, idx) => {
                  const hasHighlight = r.highlights && r.highlights[item.field];
                  const cellStyle = { padding: "4px 8px", border: "1px solid #ddd" };

                  return (
                    <tr key={idx}>
                      <td style={{ fontWeight: "bold", padding: "4px 8px", border: "1px solid #ddd", width: "180px" }}>
                        {item.label}
                      </td>
                      <td style={cellStyle}>
                        {item.field === "content" && hasHighlight
                          ? r.highlights[item.field].map((snippet, j) => (
                              <span
                                key={j}
                                dangerouslySetInnerHTML={{
                                  __html: snippet.replace(/<em>/g, '<strong style="background-color: yellow">').replace(/<\/em>/g, '</strong>')
                                }}
                              />
                            ))
                          : hasHighlight
                          ? r.highlights[item.field].map((snippet, j) => (
                              <span
                                key={j}
                                dangerouslySetInnerHTML={{
                                  __html: snippet.replace(/<em>/g, '<strong style="background-color: yellow">').replace(/<\/em>/g, '</strong>')
                                }}
                              />
                            ))
                          : item.value}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>

            <div style={{ display: "flex", justifyContent: "flex-end", marginTop: "1rem" }}>
              <Button
                variant="contained"
                color="primary"
                onClick={() => download(r.index)}
              >
                DOWNLOAD
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
</div>
  );
}

export default Search;