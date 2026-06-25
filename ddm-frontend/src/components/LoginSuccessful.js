import React from 'react'
import {Typography, Container, Box } from '@mui/material';

export default function LoginSuccessful() {
  return (
    <Container maxWidth="xs">
    <Box sx={{ mt: 8, color: 'green' }}>
      <Typography variant="h3" align="center" gutterBottom>
        You have successfully Logged in.
      </Typography>
      <Typography variant="h6" align="center" gutterBottom>
        <a href="/search">Go to search page!</a>
      </Typography>
    </Box>
    </Container>
  )
}