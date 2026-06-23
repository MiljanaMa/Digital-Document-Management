import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Typography, Button, Box, Container, Grid, TextField } from '@mui/material';
import authAxiosInstance from '../config/AuthAxiosConfig';

export default function Registration() {

    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        username: '',
        password: '',
        confirmPassword: ''
    })

    const [error, setError] = useState('')
    const navigate = useNavigate()

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };


    const handleSubmit = async (e) => {
        e.preventDefault();

        if(formData.password !== formData.confirmPassword){
            setError("Passwords do not match")
            return;
        }
        setError('')

        try{
            const response = await authAxiosInstance.post('register', formData);

            if(response.status === 201) {
                navigate('/registrationSuccessful')
            } else {
                setError('Registration failed')
            }
        } catch(err){
            setError(err.response?.data || 'An error occurred during registration')
        }
    }

return (
    <Container maxWidth="xs">
      <Box sx={{
      mt: 8,
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      width: '100%',
    }}>
        <Typography variant="h5" align="center" gutterBottom>
          Register
        </Typography>

        <form onSubmit={handleSubmit} style={{ width: '100%' }}>
          <Grid container spacing={2}>
              <TextField
                label="First Name"
                name="firstName"
                fullWidth
                value={formData.firstName}
                onChange={handleChange}
                required
              />
              <TextField
                label="Last Name"
                name="lastName"
                fullWidth
                value={formData.lastName}
                onChange={handleChange}
                required
              />
              <TextField
                label="Username"
                name="username"
                fullWidth
                value={formData.username}
                onChange={handleChange}
                required
              />
              <TextField
                label="Password"
                name="password"
                type="password"
                fullWidth
                value={formData.password}
                onChange={handleChange}
                required
              />
              <TextField
                label="Confirm Password"
                name="confirmPassword"
                type="password"
                fullWidth
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />
            {error && (
                <Typography color="error" variant="body2" align="center">
                  {error}
                </Typography>
            )}
              <Button type="submit" fullWidth variant="contained" color="primary">
                Register
              </Button>
          </Grid>
        </form>
      </Box>
    </Container>
  );
};
