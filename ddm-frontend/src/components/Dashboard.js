import React from 'react'

export default function Dashboard() {
  return (
    // eslint-disable-next-line jsx-a11y/iframe-has-title
    <iframe src="http://localhost:5601/app/dashboards#/view/7c787eb1-e9f4-4a71-b3c6-c6cee5a73b3e?_g=(refreshInterval%3A(pause%3A!t%2Cvalue%3A60000)%2Ctime%3A(from%3Anow-15M%2Cto%3Anow))&hide-filter-bar=true" 
    height="800" width="100%"></iframe>
  )
}