import { createTheme } from '@mui/material/styles';
import { green, grey } from '@mui/material/colors';

const theme = createTheme({
    palette: {
      primary: {
        main: green[800],
      },
      secondary: {
        main: grey[700], 
      }
    }
  });
  
  export default theme;