import React, { useState } from "react";
import {Button, TextField, MenuItem} from '@mui/material';
import axiosInstance from '../config/AxiosConfig';

function Search() {
  // Koji tip pretrage je aktivan
  const [searchType, setSearchType] = useState("basic"); // basic, knn, geo, advanced

  // Zajednički filteri za sve pretrage
  const [filters, setFilters] = useState({
    employeeFullName: "",
    incidentSeverity: "",
    affectedOrganizationName: "",
    securityOrganizationName: "",
    text: "",
    fullText: "",
    knn: false,
    address: "",
    radius: "",

    // za geo i advanced možeš dodati posebna polja
  });

  const [results, setResults] = useState([]);
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
    // Pripremi payload prema tipu pretrage
    let payload = {};

    // eslint-disable-next-line default-case
    switch (searchType) {
      case "basic":
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
        payload = { ...filters };
        try{
            const response = await axiosInstance.post('search/advanced', payload);
            const data = await response.json();
            setResults(data);
            setLoading(false);
        } catch(err){
            console.log("Greska prilikom dobavljanja rezultata.")
            setLoading(false);
        }
        break;
    }
  }
  async function download(doc) {
    try {
          const response = await axiosInstance.post(`file/${doc.serverFilename}`);
          const objectUrl = URL.createObjectURL(response);
          const a = document.createElement('a');
          a.href = objectUrl;
          a.download = doc.title;
          a.click();
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
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300 }}
                      value={filters.employeeName}
                      onChange={handleFilterChange}
                    />
            <TextField
                      label="Incident severity"
                      name="incidentSeverity"
                      variant="filled"
                      sx={{ mb: 3 }}
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
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.affectedOrganizationName}
                      onChange={handleFilterChange}
                    />
          <TextField
                      label="Security organization"
                      name="securityOrganizationName"
                      variant="filled"
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.securityOrganizationName}
                      onChange={handleFilterChange}
                    />
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
                      label="Radius (km)"
                      name="radius"
                      variant="filled"
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.radius}
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
                      name="text"
                      variant="filled"
                      sx={{ mb: 3 }}
                      fullWidth
                      style={{ marginRight: 10, width: 1230}}
                      value={filters.text}
                      onChange={handleFilterChange}
                    />

   
      </div>
      <div style={{ marginTop: 10, display: searchType === "advanced" ? "block" : "none" }} >
            <TextField
                      label="Boolean"
                      name="fullText"
                      variant="filled"
                      sx={{ mb: 3 }}
                      fullWidth
                      style={{ marginRight: 10}}
                      value={filters.fullText}
                      onChange={handleFilterChange}
                    />
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
            }}
          >
            <h3 style={{ color: "#3366cc", cursor: "pointer" }}>{r.title}</h3>
            <small>{r.type}</small>
            <p>
              <b>Employee:</b> {r.employeeFullName} <br />
              <b>Security organization:</b> {r.securityOrganizationName} <br />
              <b>Affected organization:</b> {r.affectedOrganizationName} <br />
              <b>Affected organization address:</b> {r.affectedOrganizationAddress}
            </p>
            <p>{r.summary}</p>
            <Button variant="contained" color="primary"
              onClick={download()}
              style={{ float: "right" }}
            >
              DOWNLOAD
            </Button>
          </div>
        ))}
      </div>
    </div>
</div>
  );
}

export default Search;