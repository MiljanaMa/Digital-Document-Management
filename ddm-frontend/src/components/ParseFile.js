import React, { useState } from "react";
import {Button, TextField, Box, Container, MenuItem} from '@mui/material';
import axiosInstance from '../config/AxiosConfig';

export default function IncidentPage() {
  const [file, setFile] = useState(null);
  const [parsedData, setParsedData] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleUpload = async () => {
    if (!file) {
      alert("Please, choose pdf file.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    setLoading(true);
    try {
      const response = await axiosInstance.post('index', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      const data = response.data
      setParsedData(data);
    } catch (error) {
      console.error(error);
      alert("Doslo je do greske.");
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async (updatedData) => {
    try {
      var response = await axiosInstance.post('index/save', updatedData);
      alert("Document is indexed!");
      setParsedData(null);
      setFile(null);
    } catch (error) {
      console.error(error);
      alert("Doslo je do greske.");
    }
  };

  const handleCancel = async (updatedData) => {
    try {
      await axiosInstance.delete(`cancel/${updatedData.dataId}`);
      alert("Document is not indexed!");
      setParsedData(null);
      setFile(null);
    } catch (error) {
      console.error(error);
      alert("Doslo je do greske.");
    }
  };

  return (
    <div className="max-w-xl mx-auto p-6">
      {!parsedData ? (
        <div className="space-y-4 bg-white p-6 shadow rounded-lg">
          <h2 className="text-lg font-bold">Document upload</h2>
          <input type="file" accept="application/pdf" onChange={handleFileChange} />
          <Button variant="contained" color="primary"
            onClick={handleUpload}
            disabled={loading}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            {loading ? "Loading..." : "Send to parse"}
          </Button>
        </div>
      ) : (
        <IncidentForm
          parsedData={parsedData}
          onConfirm={handleConfirm}
          onCancel={handleCancel}
        />
      )}
    </div>
  );
}

function IncidentForm({ parsedData, onConfirm, onCancel }) {
  const [formData, setFormData] = useState({
    employeeFullName: parsedData?.employeeFullName || "",
    securityOrganizationName: parsedData?.securityOrganizationName || "",
    affectedOrganizationName: parsedData?.affectedOrganizationName || "",
    incidentSeverity: parsedData?.incidentSeverity || "niska",
    affectedOrganizationAddress: parsedData?.affectedOrganizationAddress || "",
    title: parsedData?.title || "",
    documentId: parsedData?.documentId || "",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <Container maxWidth="xs">
    <Box sx={{
          mt: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          width: '100%',
        }}>
    <div className="mx-auto p-6 bg-white shadow-lg rounded-2xl space-y-4"
          style={{ maxWidth: '400px', justifyContent: 'center', position: 'absolute' }}>
      <h2 className="text-xl font-bold mb-4">Parsed document</h2>
      <TextField
          label="Document title"
          name="title"
          variant="filled"
          fullWidth
          sx={{ mb: 3 }}
          value={formData.title}
          InputProps={{
            readOnly: true
          }}
        />
        <TextField
          label="Employee fullname"
          name="employeeFullName"
          variant="filled"
          fullWidth
          sx={{ mb: 3 }}
          value={formData.employeeFullName}
          onChange={handleChange}
          required
        />
        <TextField
          label="Security Organization"
          name="securityOrganizationName"
          variant="filled"
          fullWidth
          sx={{ mb: 3 }}
          value={formData.securityOrganizationName}
          onChange={handleChange}
          required
        />
        <TextField
          label="Affected organization"
          name="affectedOrganizationName"
          variant="filled"
          fullWidth
          sx={{ mb: 3 }}
          value={formData.affectedOrganizationName}
          onChange={handleChange}
          required
        />
        <TextField
          label="Affected organization address"
          name="affectedOrganizationAddress"
          variant="filled"
          fullWidth
          sx={{ mb: 3 }}
          value={formData.affectedOrganizationAddress}
          onChange={handleChange}
          required
        />
        <TextField
          select
          label="Incident severity"
          name="incidentSeverity"
          variant="filled"
          fullWidth
          sx={{ mb: 3 }}
          value={formData.incidentSeverity}
          onChange={handleChange}
          required
        >
          <MenuItem value="niska">niska</MenuItem>
          <MenuItem value="srednja">srednja</MenuItem>
          <MenuItem value="visoka">visoka</MenuItem>
          <MenuItem value="kriticna">kriticna</MenuItem>
        </TextField>

      <div className="flex justify-end gap-4 mt-6">
        <Button variant="contained" color="primary"
          onClick={() => onConfirm(formData)} sx={{ mr: 3, width: 100 }}
          className="px-4 py-2 bg-gray-300 rounded-lg hover:bg-gray-400"
        >
          Cancel
        </Button>
        <Button variant="contained" color="primary"
          onClick={() => onConfirm(formData)} sx={{ width: 100 }}
          className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
        >
          Save
        </Button>
      </div>
    </div>
    </Box>
    </Container>
  );
}
